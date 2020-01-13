# JPA Paging Reader 정리

> 해당 글은 [처음으로 배우는 스프링 부트 2](http://www.hanbit.co.kr/store/books/look.php?p_code=B4458049183), [기억보단 기록을 : Spring Batch Paging Reader 사용시 같은 조건의 데이터를 읽고 수정할때 문제](https://jojoldu.tistory.com/337)을 보고 학습한 내용을 정리한 글입니다.


## JpaPagingItemReader
JpaPagingItemReader에는 지정한 setPageSize 크기만큼 데이터베이스에서 읽어옵니다.(대부분 CHUNK_SZIE와 맞추는 게 좋을 거 같다.) 즉 모든 데이터를 가져와서 처리하는 방식이 아닌 paging 처리만큼 가져와서 처리하는 방식입니다.


```kotlin
@Bean(destroyMethod = "") // (1)
@StepScope
fun orderPagingReader(): JpaPagingItemReader<Order> {
    val itemReader = object : JpaPagingItemReader<Order>() {
        override fun getPage(): Int {
            return 0
        }
    }
    itemReader.setQueryString("select o from Order o where o.amount > :targetAmount") //(2)
    itemReader.pageSize = CHUNK_SZIE // (4)
    itemReader.setEntityManagerFactory(entityManagerFactory)
    val parameterValues = HashMap<String, Any>() //(3)
    parameterValues["targetAmount"] = BigDecimal("1000.00")
    itemReader.setParameterValues(parameterValues)
    itemReader.setName("orderPagingReader")

    return itemReader
}
```

* (1) 스프링에서 destroyMethod를 사용해 삭제할 빈을 자동으로 추적합니다. `destroyMethod=""`으로 설정하여 기능을 사용하지 않도록 설정합니다. (warning)
* (2) JpaPagingReader를 사용하려면 쿼리를 직접 문자열로 사용할 수밖에 없습니다.
* (3) Query에 값을 바인딩 시킬 경우 Map을 시용해서 넘겨줍니다.
* (4) chunk size를 설정합니다. 여기서 chunk size는 읽을 데이터의 개수를 의미합니다. 정확히는 offset, limit에서 limit을 의미합니다. 읽어온 사이즈만큼 wirter에서 update를 발생시키게 합니다.


## JpaPagingItemReader 문제점
전체에 대한 페이징은 이슈가 없지만 특정 Flag 값을 기준으로 JpaPagingItemReader를 사용하면 문제가 발생할 수도 있습니다.


id | amount
---|-------
1  | 101.00
2  | 111.00
3  | 11.00
4  | 1002.00
5  | 1002.00
6  | 1230.00


위처럼 데이터가 있을 때 1000.00 보다 작은 amount를 1209.11으로 변경하는 Job을 offset limit 방식으로 구현했을 경우

```sql
SELECT *
FROM orders
where amount < 1000.00
limit 0, 2
```
아래처럼 데이터가 변경된됩니다.

id | amount
---|-------
1  | 1209.11
2  | 1209.11
3  | 11.00
4  | 1002.00
5  | 1002.00
6  | 1230.00

다시 쿼리를 실행하면 
```sql
SELECT *
FROM orders
where amount < 1000.00
limit 2, 2
```

우리가 원하는 데이터는 id 3, 4번이지만 실제로는 5, 6 번이 나옵니다. 1, 2번이 조회 대상에서 제외되면서 `limit 0, 1`이 3, 4 번이 되기 때문입니다.

### 해결 방법 1 : page를 초기화 하기 

```kotlin
@Bean(destroyMethod = "")
@StepScope
fun orderPagingReader(): JpaPagingItemReader<Order> {
    val itemReader = object : JpaPagingItemReader<Order>() {
        override fun getPage(): Int {
            return 0
        }
    }
    itemReader.setQueryString("select o from Order o where o.amount < :targetAmount")
    itemReader.pageSize = CHUNK_SZIE
    itemReader.setEntityManagerFactory(entityManagerFactory)
    val parameterValues = HashMap<String, Any>()
    parameterValues["targetAmount"] = BigDecimal("1000.00")
    itemReader.setParameterValues(parameterValues)
    itemReader.setName("orderPagingReader")

    return itemReader
}
```
getPage 값을 0으로 계속 설정하는 것입니다. 그렇다면 아래처럼 SQL이 변경됩니다.


```sql
SELECT *
FROM orders
where amount < 1000.00
limit 2
```
`offset`에 해당하는 쿼리가 발동을 하지 않습니다. 그렇게 되면 데이터 변경으로 인한 조회대상이 사라지지 않고 계속 limit 만큼 조회할 수 있습니다.


```
Hibernate: select order0_.id as id1_0_, order0_.amount as amount2_0_, order0_.created_at as created_3_0_, order0_.updated_at as updated_4_0_ from orders order0_ where order0_.amount<? limit ?
```
위 로그는 실제 SQL 로그입니다. limit ? 쿼리가 출력되시는 것을 확인할 수 있습니다.