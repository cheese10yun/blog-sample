> [Spring Batch 가이드 - 8. ItemWriter](https://github.com/jojoldu/spring-batch-in-action/blob/master/8_WRITER.md)을 보고 정리한 글입니다.

Writer는 Reader, Processor와 함께 ChunkOrientedTasklet을 구성하는 3 요소입니다. 여기서 Processor가 아닌 Writer를 우선 선택한 이유는 **Processor는 선택이기 때문입니다. Processor가
없어도 ChunkOrientedTasklet을 구성할 수 있습니다. 반면 Reader, Writer는 ChunkOrientedTasklet에서 필수로 구성해야 합니다.**

Spring Batch가 처음 나왔을 때, **ItemWriter는 ItemReader와 마찬가지로 item을 하나씩 다루었습니다.** 그러나 Spring Batch2와 Chunk 기반 처리의 도입으로 인해 ItemWriter의 큰 변경이
있었습니다. **ItemWriter는 item 하나를 작성하지 않고 Chunk 단위로 묶인 item List를 다룹니다.** 이 때문에 ItemWriter 인터페이스는 ItemReader 인터페이스와 약간 다릅니다.

```java
public interface ItemReader<T> {
        ...
	@Nullable
	T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException;
}

public interface ItemWriter<T> {

	/**
	 * Process the supplied data element. Will not be called with any null items
	 * in normal operation.
	 *
	 * @param items items to be written
	 * @throws Exception if there are errors. The framework will catch the
	 * exception and convert or rethrow it as appropriate.
	 */
	void write(List<? extends T> items) throws Exception;
}
```

ItemReader는 `read()` 하나를 반환하는 반면, ItemWriter의 write()는 인자로 item list를 받습니다.

![](https://github.com/jojoldu/spring-batch-in-action/raw/master/images/8/write-process.png)

> 이미지 출처 [Spring Batch 가이드 - 8. ItemWriter](https://github.com/jojoldu/spring-batch-in-action/blob/master/8_WRITER.md)

* ItemReader를 통해 각 항목을 개별적으로 읽고 이를 처리하기 위해 ItemProcessor에 전달합니다.
* **이 프로세스는 청크의 Item 개수 만큼 처리될때까지 지속됩니다.**
* **청크 단위만큼 처리가 완료되면 Writer에 전달되어 Writer에 명시되어있는 대로 일괄 처리합니다.**

Spring Batch는 JDBC와 ORM 모두 Writet를 제공합니다. **Writer는 Chunk단위의 마지막 단계이기 때문에 Database의 영속성과 관련해서 항상 마지막에 Flush를 발생시켜야 합니다.**

예를 들어 아래와 같이 영속성을 사용하는 **JPA, Hibernate의 경우 ItemWriter 구현체에서는 `flush()`와 `session.clear()`가 따라옵니다.**

```java
// JpaItemWriter 
@Override
public void write(List<? extends T> items) {
    EntityManager entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);
    if (entityManager == null) {
        throw new DataAccessResourceFailureException("Unable to obtain a transactional EntityManager");
    }
    doWrite(entityManager, items);
    entityManager.flush();
}

// HibernateItemWriter
@Override
public void write(List<? extends T> items) {
    doWrite(sessionFactory, items);
    sessionFactory.getCurrentSession().flush();
    if(clearSession) {
        sessionFactory.getCurrentSession().clear();
    }
}
```

**Writer가 받은 모든 Item이 처리 된 후, Spring Batch는 현재 트랜잭션을 커밋합니다.** 데이터베이스와 관련된 Writer는 3가지가 있습니다.

* JdbcBatchItemWriter
* HibernateBatchItemWriter
* JpaItemWriter

## JdbcBatchItemWriter

ORM을 사용하지 않은 경우 Writer는 대부분 JdbcBatchItemWriter를 사용합니다. 이 JdbcBatchItemWriter는 아래 그림과 같이 JDBC의 Batch 기능을 사용하여 한 번에 Database로 전달하여 Database
내부에 쿼리를 실행되도록 합니다.

![](https://github.com/cheese10yun/TIL/blob/master/assets/jdbc-batch-item-wirter.png?raw=true)

**이렇게 ChunkSzie 만큼 쌓아 Query를 한번에 전송하기 때문에 애플리케이션과 데이터베이스 간의 데이터 통신의 최소화 시켜 성능을 향상 시킬수 있습니다.** 업데이트 쿼리 또한 마찬 가지입니다. 업데이트를 일괄 처리로 그룹화하면
데이터베이스와 어플리케이션간 왕복 횟수가 줄어들어 성능이 향상 됩니다.

```kotlin
@Configuration
class JdbcBatchItemWriterJobConfiguration(
        private val jobBuilderFactory: JobBuilderFactory,
        private val stepBuilderFactory: StepBuilderFactory,
        private val dataSource: DataSource
) {

    private val chunkSize = 10

    @Bean
    fun jdbcBatchWriterJob(): Job {
        return jobBuilderFactory.get("jdbcBatchWriterJob")
                .start(jdbcBatchItemWriterStep())
                .build();
    }

    @Bean
    fun jdbcBatchItemWriterStep(): Step {
        return stepBuilderFactory.get("jdbcBatchItemWriterStep")
                .chunk<Order, Order>(chunkSize)
                .reader(jdbcBatchItemWriterReader())
                .writer(jdbcBatchItemWriter())
                .build()
    }


    @Bean
    fun jdbcBatchItemWriterReader(): JdbcCursorItemReader<Order> {
        return JdbcCursorItemReaderBuilder<Order>()
                .fetchSize(chunkSize)
                .dataSource(dataSource)
                .rowMapper(BeanPropertyRowMapper(Order::class.java))
                .sql("select `id` ,`amount`, `created_at`, `updated_at` from orders")
                .name("jdbcBatchItemWriterReader")
                .build()

    }

    @Bean
    fun jdbcBatchItemWriter(): JdbcBatchItemWriter<Order> {
        return JdbcBatchItemWriterBuilder<Order>()
                .dataSource(dataSource)
                .sql("insert into orders (amount, created_at, updated_at) values (:amount, :created_at, :updated_at)")
                .beanMapped()
                .build();

    }
}
```

Spring Batch를 처음 쓰시는 분들이 자주 오해하시는게 이 부분입니다. 위 코드에서도 나와있지만, Pay2 테이블에 데이터를 넣은 Writer이지만 선언된 제네릭 타입은 Reader/Processor에서 넘겨준 Pay클래스입니다.

## JpaItemWriter

Writer는 ORM을 사용할 수 있는 `JpaItemWriter`입니다.

```kotlin
@Configuration
class JpaItemWriterJobConfiguration(
        private val jobBuilderFactory: JobBuilderFactory,
        private val stepBuilderFactory: StepBuilderFactory,
        private val entityManagerFactory: EntityManagerFactory
) {

    private val chunkSize = 10

    @Bean
    fun jpaItemWriterJob(): Job {
        return jobBuilderFactory.get("jpaItemWriterJob")
                .incrementer(RunIdIncrementer())
                .start(jpaItemWriterStep())
                .build()
    }

    @Bean
    fun jpaItemWriterStep(): Step {
        return stepBuilderFactory.get("jpaItemWriterStep")
                .chunk<Order, Order2>(10)
                .reader(jpaItemWriterReader())
                .processor(jpaItemProcessor())
                .writer(jpaItemWriter())
                .build()
    }

    @Bean
    fun jpaItemWriterReader(): JpaPagingItemReader<Order> {
        return JpaPagingItemReaderBuilder<Order>()
                .name("jpaItemWriterReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT o From Order o")
                .build()
    }

    @Bean
    fun jpaItemProcessor(): ItemProcessor<Order, Order2> {
        return ItemProcessor { order: Order -> Order2(order.amount) }
    }

    @Bean
    fun jpaItemWriter(): JpaItemWriter<Order2> {
        val itemWriter = JpaItemWriter<Order2>()
        itemWriter.setEntityManagerFactory(entityManagerFactory)
        return itemWriter;
    }
}
```

JpaItemWriter는 JPA를 사용하기 때문에 영속성 관리를 위해 EntityManager를 할당해줘야 합니다. 여기서 processor가 추가되었습니다. Order Entity를 읽어서 Writer에는 Order2 Entity를 전달해주기 위함
입니다.

JpaItemWriter는 JdbcBatchItemWriter와 달리 **넘어온 Entity를 데이터베이스에 반영합니다.** 즉, JpaItemWriter는 Entity 클래스를 제네릭 타입으로 받아야만 합니다.

```java
protected void doWrite(EntityManager entityManager, List<? extends T> items) {

    if (logger.isDebugEnabled()) {
        logger.debug("Writing to JPA with " + items.size() + " items.");
    }

    if (!items.isEmpty()) {
        long mergeCount = 0;
        for (T item : items) {
            if (!entityManager.contains(item)) {
                entityManager.merge(item);
                mergeCount++;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(mergeCount + " entities merged.");
            logger.debug((items.size() - mergeCount) + " entities found in persistence context.");
        }
    }

}
```

당연한 이야기겠지만 JPA 에서는 영속성 컨텍스트에 저장하기 위해서는 해당 객체가 엔티티 클래스여야 하기 때문에 `entityManager.merge(item);` 으로 저장됩니다.

## Custom ItemWriter

Reader와 달리 Writer의 경우 Custom하게 구현해야할 일이 많습니다. 물론 Reader 역시 Custom 할일이 많습니다.

예를 들어 다음과 같은 경우가 있습니다.

* Reader에서 읽어온 데이터를 RestTemplate으로 외부 API로 전달해야할때
* 임시저장을 하고 비교하기 위해 싱글톤 객체에 값을 넣어야할때
* 여러 Entity를 동시에 save 해야할때

```kotlin
@Bean
fun customItemWriter(): ItemWriter<Order2> {
    return ItemWriter { items ->
        for (item in items) {
            println(item.amount)
        }
    }
}
```