> [Spring Batch 가이드 - 7. ItemReader](https://github.com/jojoldu/spring-batch-in-action/blob/master/6_CHUNK.md)을 보고 정리한 글입니다.

# ItemRader
기존에 Step에서는 Tasklet 단위로 처리되고, Tasklet 중에서 ChunkOrientedTasklet를 통해 Chunk를 처리하며 이를 구성하는 3요소로 ItemReader, ItemWProcessor, ItemWriter가 있었습니다.

## ItemRader 소개
![](https://github.com/cheese10yun/TIL/blob/master/assets/spring-batch-item-reader.png?raw=true)


 Spring Batch의 ItemReader는 데이터를 읽어들입니다. **그게 꼭 DB의 데이터만을 얘기하진 않습니다.** File, XML, JSOM. CSV. Excel 등 다른 데이터 소스를 배치 처리의 입력으로 사용할 수 있습니다. 

 **이외에도 Spring Batch에서 지원하지 않은 Reader가 필요할 경우 직접 Reader를 만들수도 있습니다.**

 먼저 ItemReader를 살펴보면 **read() 만 가지고 있습니다.**

 ```java
 public interface ItemReader<T> {

	/**
	 * Reads a piece of input data and advance to the next one. Implementations
	 * <strong>must</strong> return <code>null</code> at the end of the input
	 * data set. In a transactional setting, caller might get the same item
	 * twice from successive calls (or otherwise), if the first call was in a
	 * transaction that rolled back.
	 * 
	 * @throws ParseException if there is a problem parsing the current record
	 * (but the next one may still be valid)
	 * @throws NonTransientResourceException if there is a fatal exception in
	 * the underlying resource. After throwing this exception implementations
	 * should endeavour to return null from subsequent calls to read.
	 * @throws UnexpectedInputException if there is an uncategorised problem
	 * with the input data. Assume potentially transient, so subsequent calls to
	 * read might succeed.
	 * @throws Exception if an there is a non-specific error.
	 * @return T the item to be processed or {@code null} if the data source is
	 * exhausted
	 */
	@Nullable
	T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException;
}
 ```

 ItemStream 인터페이스는 주기적으로 상태를 저장하고 오류가 발생하면 해당 상태에서 복원하기 위한 마커 인터페이스입니다. 즉, **배치 프로세스의 실행 컨텍스트와 연계해서 ItemReader의 상태를 저장하고 실패한 곳에서 다시실행할 수 있게 해주는 역할을 합니다.**

 ```java
 public interface ItemStream {

	/**
	 * Open the stream for the provided {@link ExecutionContext}.
	 *
	 * @param executionContext current step's {@link org.springframework.batch.item.ExecutionContext}.  Will be the
	 *                            executionContext from the last run of the step on a restart.
	 * @throws IllegalArgumentException if context is null
	 */
	void open(ExecutionContext executionContext) throws ItemStreamException;

	/**
	 * Indicates that the execution context provided during open is about to be saved. If any state is remaining, but
	 * has not been put in the context, it should be added here.
	 * 
	 * @param executionContext to be updated
	 * @throws IllegalArgumentException if executionContext is null.
	 */
	void update(ExecutionContext executionContext) throws ItemStreamException;

	/**
	 * If any resources are needed for the stream to operate they need to be destroyed here. Once this method has been
	 * called all other methods (except open) may throw an exception.
	 */
	void close() throws ItemStreamException;
}
 ```
ItemStream의 3개 메소드는 다음과 같은 역할을 합니다.
* open(), close()는 스트림을 열고 닫습니다.
* update()를 사용하면 Batch 처리의 상태를 업데이트 할 수 있습니다.

### JpaPagingItemReader
Spring Batch JPA를 지원하기 위해 JpaPagingItemReader를 공식적으로 지원하고 있습니다.

> 현재 Querydsl, Jooq 등을 통한 ItemReader 구현체는 공식 지원하지 않습니다. CustomItemReader 구현체를 만드셔야만 합니다.
이건 다른 글을 통해서 소개 드리겠습니다.
당장 필요하신 분들은 공식 문서를 참고해보세요

JPA는 Hibernate와 많은 유사점을 가지고 있습니다만, 한가지 다른 것이 있다면 **Hibernate 에선 Cursor가 지원되지만 JPA에는 Cursor 기반 Database 접근을 지원하지 않습니다.**

**PagingItemReader 주의 사항 정렬 (Order) 가 무조건 포함되어 있어야 합니다.**

### RepositoryItemReader
RepositoryItemReader는 PagingAndSortingRepository를 이용한 ItemReader 구현체입니다. JpaPagingItemReader와 동일한 방법로 Builder로 구성하며 구현이 보다 쉬운 장점이 있습니다.


```kotlin
@Configuration
class RepositoryItemReaderJobConfiguration(
        private val jobBuilderFactory: JobBuilderFactory,
        private val stepBuilderFactory: StepBuilderFactory,
        private val orderRepository: OrderRepository
) {
    private val chunkSize = 100
    private val log by logger()

    @Bean
    fun repositoryItemReaderJob(): Job {
        return jobBuilderFactory
                .get("repositoryItemReaderJob")
                .incrementer(RunIdIncrementer())
                .start(step())
                .build()
    }

    fun step(): Step {
        return stepBuilderFactory
                .get("step")
                .chunk<Order, Order>(chunkSize)
                .reader(reader())
                .processor(processor())
                .writer(write())
                .build()
    }


    private fun reader(): RepositoryItemReader<Order> {
        return RepositoryItemReaderBuilder<Order>()
                .name("reader")
                .repository(orderRepository)
                .methodName("findByAmountGreaterThan")
                .arguments(listOf(BigDecimal.ZERO))
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .saveState(false)
                .pageSize(chunkSize)
                .build()
    }

    private fun processor(): ItemProcessor<Order, Order> {
        return ItemProcessor {
            log.info("ItemProcessor ->>>>>>>>>>>>>>>> ${it.amount}")
            it
        }

    }


    private fun write(): ItemWriter<Order> {
        return ItemWriter {
            for (order in it) {
                log.info("ItemWriter ->>>>>>>>>>>>>>> ${order.amount}")
            }
        }
    }
}
```

**사용시 주의해야할점은 반드시 sorts 정보를 해야하며, methodName()의 메서드 즉, Query Method 리턴 타입이 Page 이어야 한다는 것이다.**

```kotlin
interface OrderRepository : JpaRepository<Order, Long> {
    fun findByAmountGreaterThan(amount: BigDecimal, pageable: Pageable) : Page<Order>
}
```
`JpaPagingItemReader` 보다는 구현이 쉽긴 하지만 `methodName` 방식은 type safe 하지 않기 때문에 결국 ItemReader는 직접 구현해서 사용하는 방식이 적절한거 같다.




## JpaPagingItemReader 더 살펴 보기
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