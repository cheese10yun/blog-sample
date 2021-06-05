# Spring Batch Reader 성능 분석

## Item Reader
데이터 베이스에서 데이터를 읽어 대량의 데이터를 `read`해서 진행하는 경우 성능적인 차이와, 각 리더의 특징과 어느 부분에서 사용해야 하는지에 대해서 정리해보았습니다.

### 조회 대상

```sql
# created_at 인덱스
CREATE TABLE `payment`
(
    `id`         bigint(20)     NOT NULL AUTO_INCREMENT,
    `amount`     decimal(19, 2) NOT NULL,
    `created_at` datetime       NOT NULL,
    `order_id`   bigint(20)     NOT NULL,
    `updated_at` datetime       NOT NULL,
    PRIMARY KEY (`id`),
    KEY `IDXfxl3u00ue9kdoqelvslc1tj6h` (`created_at`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
```

가장 최근 데이터부터 데이터를 읽어 특정 날짜까지 데이터를 읽는 기준으로 Item Reader를 작성했습니다.

### 대상 리더
* [JpaPagingItemReader](https://docs.spring.io/spring-batch/docs/current/api/org/springframework/batch/item/database/JpaPagingItemReader.html)
* [QueryDslNoOffsetPagingReader](https://jojoldu.tistory.com/473?category=902551)
* [HibernateCursorItemReader]()

### 성능

### 전체 성능 표
Reader | rows | Chunk Size | 소요 시간(ms)
-------|------|------------|----------
JpaPagingItemReader | 10,000 | 1000 | 778 | 
JpaPagingItemReader | 50,000 | 1000 | 3243 | 
JpaPagingItemReader | 100,000 | 1000 | 8912 | 
JpaPagingItemReader | 500,000 | 1000 | 205469 | 
JpaPagingItemReader | 1,000,000 | 1000 | 1048979
JpaPagingItemReader | 5,000,000 | 1000 | ...
QueryDslNoOffsetPagingReader | 10,000 | 1000 | 658 | 
QueryDslNoOffsetPagingReader | 50,000 | 1000 | 2004 | 
QueryDslNoOffsetPagingReader | 100,000 | 1000 | 3523 | 
QueryDslNoOffsetPagingReader | 500,000 | 1000 | 15501 | 
QueryDslNoOffsetPagingReader | 1,000,000 | 1000 | 28732
QueryDslNoOffsetPagingReader | 5,000,000 | 1000 | 165249
HibernateCursorItemReader | 10,000 | 1000 | 448 | 
HibernateCursorItemReader | 50,000 | 1000 | 1605 | 
HibernateCursorItemReader | 100,000 | 1000 | 2886 | 
HibernateCursorItemReader | 500,000 | 1000 | 17411 | 
HibernateCursorItemReader | 1,000,000 | 1000 | 25439 | 
HibernateCursorItemReader | 5,000,000 | 1000 | 132552

`JpaPagingItemReader`의 rows `5,000,000` 측정은 너무 걸려 측정하지 못했습니다. 대략 5시간 이상까지 측정하다 종료했습니다.

### 성능 그래프

![](img/reader-performance-1.png)

![](img/reader-performance.png)

`JpaPagingItemReader` 리더의 시간이 너무 오래 소요되어  `QueryDslNoOffsetPagingReader`, `HibernateCursorItemReader` 리더 비교

### 전체 코드

```kotlin
@Configuration
class ReaderPerformanceJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    val log by logger()

    @Bean
    fun readerPerformanceJob(
        jobDataSetUpListener: JobDataSetUpListener,
        readerPerformanceStep: Step
    ) = jobBuilderFactory["readerPerformanceJob"]
            .incrementer(RunIdIncrementer())
            .start(readerPerformanceStep)
            .build()

    @Bean
    @JobScope
    fun readerPerformanceStep(
        jpaPagingItemReader: JpaPagingItemReader<Payment>,
        hibernateCursorItemReader: HibernateCursorItemReader<Payment>,
        queryDslNoOffsetPagingReader: QuerydslNoOffsetPagingItemReader<Payment>
    ) =
        stepBuilderFactory["readerPerformanceStep"]
            .chunk<Payment, Payment>(CHUNK_SIZE)
// 해당 Reader 중 택 1
//            .reader(jpaPagingItemReader)
//            .reader(queryDslNoOffsetPagingReader)
//            .reader(hibernateCursorItemReader)
            .writer { log.info("item size ${it.size}") }
            .build()

    @Bean
    @StepScope
    fun jpaPagingItemReader(
        entityManagerFactory: EntityManagerFactory
    ) = JpaPagingItemReaderBuilder<Payment>()
        .name("jpaPagingItemReader")
        .pageSize(CHUNK_SIZE)
        .entityManagerFactory(entityManagerFactory)
        .queryString("SELECT p FROM Payment p where p.createdAt >= :createdAt ORDER BY p.createdAt DESC")
        .parameterValues(mapOf("createdAt" to LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)))
        .build()
    
    @Bean
    @StepScope
    fun queryDslNoOffsetPagingReader(
        entityManagerFactory: EntityManagerFactory
    ): QuerydslNoOffsetPagingItemReader<Payment> {
        // 1. No Offset Option
        val options = QuerydslNoOffsetNumberOptions<Payment, Long>(qPayment.id, Expression.ASC)
        // 2. Querydsl Reader
        return QuerydslNoOffsetPagingItemReader(entityManagerFactory, CHUNK_SIZE, options) {
            it.selectFrom(qPayment)
                .where(qPayment.createdAt.goe(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)))
        }
    }

    @Bean
    @StepScope
    fun hibernateCursorItemReader(
        sessionFactory: SessionFactory
    ) = HibernateCursorItemReaderBuilder<Payment>()
        .name("hibernateCursorItemReader")
        .fetchSize()
        .sessionFactory(sessionFactory)
        .queryString("SELECT p FROM Payment p where p.createdAt >= :createdAt ORDER BY p.createdAt DESC")
        .parameterValues(mapOf("createdAt" to LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)))
        .build()
}
```

### JpaPagingItemReader

`JpaPagingItemReader`눈 Spring Batch에서 제공해 주는 ItemReader로 일반적인 페이징 방식을 통해서 가져오는 방식입니다. 모든 데이터를 한 번에 가져와서 처리할 수는 없으니 `offset`, `limit` 방식으로 데이터를 가져옵니다. 실제 쿼리는 다음과 같습니다.

```sql
select payment0_.id         as id1_0_,
       payment0_.amount     as amount2_0_,
       payment0_.created_at as created_3_0_,
       payment0_.order_id   as order_id4_0_,
       payment0_.updated_at as updated_5_0_
from payment payment0_
where payment0_.created_at >= ?
order by payment0_.created_at DESC
limit ?, ?
```

해당 리더는 위 그래프에서 확인했듯이 다른 리더에 비해서 현저하게 드립니다. **읽어야 할 총 데이터가 많고, 청크 후반으로 갈수록 더욱 느려집니다.**

#### 1 ~ 2 번째 조회
```sql
Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.created_at as created_3_0_, payment0_.order_id as order_id4_0_, payment0_.updated_at as updated_5_0_ from payment payment0_ where payment0_.created_at>=? order by payment0_.created_at DESC limit ?
2021-05-25 22:40:25.963  INFO 93165 --- [           main] uration$$EnhancerBySpringCGLIB$$d8232fb2 : item size 1000
Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.created_at as created_3_0_, payment0_.order_id as order_id4_0_, payment0_.updated_at as updated_5_0_ from payment payment0_ where payment0_.created_at>=? order by payment0_.created_at DESC limit ?, ?
2021-05-25 22:40:26.016  INFO 93165 --- [           main] uration$$EnhancerBySpringCGLIB$$d8232fb2 : item size 1000

```
첫 조회 이후 두 번째 조회까지의 시간은 `26.016 - 25.963 = 0.053`의 짧은 시간밖에 걸리지 않았습니다.

#### 49,990,000 ~ 5,000,0000 조회

```
2021-05-31 02:24:27.943  INFO 13475 --- [           main] uration$$EnhancerBySpringCGLIB$$4d92f8c5 : item size 1000
Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.created_at as created_3_0_, payment0_.order_id as order_id4_0_, payment0_.updated_at as updated_5_0_ from payment payment0_ where payment0_.created_at>=? order by payment0_.created_at DESC limit ?, ?
2021-05-31 02:25:18.092  INFO 13475 --- [           main] uration$$EnhancerBySpringCGLIB$$4d92f8c5 : item size 1000
Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.created_at as created_3_0_, payment0_.order_id as order_id4_0_, payment0_.updated_at as updated_5_0_ from payment payment0_ where payment0_.created_at>=? order by payment0_.created_at DESC limit ?, ?
```

마지막 청크 사이즈 조회하는 시간은 `25:18.092 - 24:27.943` 대략 51초가 걸렸습니다. **즉 해당 리더는 초반 청크는 빠르지만 후반으로 갈수록 청크를 읽는 부분이 느려지며, 데이터가 많으면 많을수록 더 느려지는 것을 확인할 수 있습니다.**

#### 왜 후반 리드에서 느려지는 것일까?

#### explain: 첫 청크 
```sql
explain select payment0_.id         as id1_0_,
       payment0_.amount     as amount2_0_,
       payment0_.created_at as created_3_0_,
       payment0_.order_id   as order_id4_0_,
       payment0_.updated_at as updated_5_0_
from payment payment0_
where payment0_.created_at >= '2021-05-30 00:00:00'
order by payment0_.created_at DESC
limit 1000;
```

| id | select\_type | table | partitions | type | possible\_keys | key | key\_len | ref | rows | filtered | Extra |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | SIMPLE | payment0\_ | NULL | range | IDXfxl3u00ue9kdoqelvslc1tj6h | IDXfxl3u00ue9kdoqelvslc1tj6h | 5 | NULL | 2306025 | 100 | Using index condition |


![](img/explain_1.png)

* `type: range` 인덱스 특정 범위의 행에 접근, 즉 인덱스가 제대로 동작
* `possible_keys: IDXfxl3u00ue9kdoqelvslc1tj6h(created_at)`  이용 가능성 있는 인덱스의 목록
* `key: IDXfxl3u00ue9kdoqelvslc1tj6h(created_at)`: `possible_keys` 필드는 이용 가능성 있는 인덱스의 목록 중에서 실제로 옵티마이저가 선택한 인덱스의 key
* `rows: 2306025` 특정 rows을 찾기 위해 읽어야 하는 MySQL 예상 rows, 단 어디까지나 통계 값으로 계산한 값이므로 실제 rows 수와 반드시 일치하지 않는다.
* `filtered: 100` rows 데이터를 가져와 WHERE 구의 검색 조건이 적용되면 몇행이 남는지를 표시, 이 값도 통계 값 바탕으로 계산한 값이므로 현실의 값과 반드시 일치하지 않는다.
* `Extra: Using index condition` 인덱스 컨디션 pushdown(ICP) 최적화가 진행(`Using index`와 비슷한 개념으로 인덱스에서만 접근해서 쿼티를 해결하는 것을 의미, 정확히 알고 있는 개념은 아니라서 [Index Condition Pushdown Optimization](https://dev.mysql.com/doc/refman/5.6/en/index-condition-pushdown-optimization.html)참고)

해당 실행 계획을 정리하면 `created_at` 인덱스가 `type: range`로 제대로 동작했습니다. 하지만 `rows: 2306025`인 것을 봐서 상당히 많은 rows를 읽은 이후에 해당 rows를 찾는 거 같습니다. 대략 `limit 2000000, 1000`까지는 첫 청크 인덱스와 동일하게 `type: range`의 실행 계획을 가졌습니다.

#### explain: 마지막 청크

```sql
explain select payment0_.id         as id1_0_,
       payment0_.amount     as amount2_0_,
       payment0_.created_at as created_3_0_,
       payment0_.order_id   as order_id4_0_,
       payment0_.updated_at as updated_5_0_
from payment payment0_
where payment0_.created_at >= '2021-05-30 00:00:00'
order by payment0_.created_at DESC
limit 4999000, 1000;
```

| id | select\_type | table | partitions | type | possible\_keys | key | key\_len | ref | rows | filtered | Extra |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | SIMPLE | payment0\_ | NULL | ALL | IDXfxl3u00ue9kdoqelvslc1tj6h | NULL | NULL | NULL | 4612051 | 50 | Using where; Using filesort |

![](img/explan_2.png)

* `type: ALL` **풀 스캔, 테이블의 데이터 전체에 접근**
* `key: IDXfxl3u00ue9kdoqelvslc1tj6h(created_at)`: `possible_keys` 필드를 이용하지 않음, 즉 인덱스 사용 안 함
* `Extra`
  * `Using where` **테이블에서 행을 가져온 후 추가적으로 검색 조건을 적용해 행의 범위를 축소**
  * `Using filesort` **ORDER BY 인덱스로 해결하지 못하고, filesort(MySQL의 quick sort)로 행을 정렬**

특정 청크 이후부터는 index를 타지 못하고 풀 스캔이 진행되고 있습니다. 당연히 해당 쿼리는 느릴 수밖에 없습니다.

### QueryDslNoOffsetPagingReader

QueryDslNoOffsetPagingReader는 [Spring Batch QuerydslItemReader](https://github.com/jojoldu/spring-batch-querydsl) 오픈소스 코드로 자세한 내용은 [기억보단 기록을: Spring Batch와 QuerydslItemReader](https://jojoldu.tistory.com/473?category=902551)에 정리되어 있습니다.

해당 내용을 간단하게 정리하면 다음과 같습니다.

1. `where AND id < 마지막 조회 ID` 조건으로 균일할 리드 속도를 보장
2. 복잡한 정렬 기준이(group by, 집계 쿼리 등등) 있는 경우 사용 불가능
3. 대량의 페이지 조회에 적합 (개인적으로 대력 5만 건 이상의 경우 사용이 적합하다고 생각합니다.)

#### explain: 첫 청크 

```sql
explain
select payment0_.id         as id1_0_,
       payment0_.amount     as amount2_0_,
       payment0_.created_at as created_3_0_,
       payment0_.order_id   as order_id4_0_,
       payment0_.updated_at as updated_5_0_
from payment payment0_
where payment0_.created_at >= '2021-05-30 00:00:00'
  and payment0_.id >= 1
  and payment0_.id <= 1000
order by payment0_.id asc
limit 1000;
```
| id | select\_type | table | partitions | type | possible\_keys | key | key\_len | ref | rows | filtered | Extra |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | SIMPLE | payment0\_ | NULL | range | PRIMARY,IDXfxl3u00ue9kdoqelvslc1tj6h | PRIMARY | 8 | NULL | 1000 | 50 | Using where |

![](img/explain_3.png)


#### explain: 마지막 청크 
```sql
explain
select payment0_.id         as id1_0_,
       payment0_.amount     as amount2_0_,
       payment0_.created_at as created_3_0_,
       payment0_.order_id   as order_id4_0_,
       payment0_.updated_at as updated_5_0_
from payment payment0_
where payment0_.created_at >= '2021-05-30 00:00:00'
  and payment0_.id >= 4999001
  and payment0_.id <= 5000000
order by payment0_.id asc
limit 1000;
```

| id | select\_type | table | partitions | type | possible\_keys | key | key\_len | ref | rows | filtered | Extra |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | SIMPLE | payment0\_ | NULL | range | PRIMARY,IDXfxl3u00ue9kdoqelvslc1tj6h | PRIMARY | 8 | NULL | 1000 | 50 | Using where |
![](img/explain_4.png)

* `type: range` 인덱스 특정 범위의 행에 접근, 즉 인덱스가 제대로 동작
* `possible_keys: PRIMARY, IDXfxl3u00ue9kdoqelvslc1tj6h(created_at)`는 `id`, `created_at` 칼럼 인덱스로 사용 가능
* `key: PRIMARY`: `possible_keys` 필드 중 `id`를 인덱스로 사용
* `Extra: Using where` **테이블에서 행을 가져온 후 추가적으로 검색 조건을 적용해 행의 범위를 축소**

**첫 청크와 마지막 청크의 실행 계획이 동일한 것을 확인할 수 있습니다. 즉 해당 리더는 청크 사이즈, 데이터 총 rows와 별개로 조회 시간이 균일합니다.**

### HibernateCursorItemReader

나중에 추가하겠음
