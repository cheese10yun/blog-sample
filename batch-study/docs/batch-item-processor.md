> [Spring Batch 가이드 - 9. Spring Batch 가이드 - ItemProcessor](https://jojoldu.tistory.com/347)을 보고 정리한 글입니다.

# Item Processor

`ItemProcessor`는 데이터 가공을 담당하는 역할을 합니다. 데이터 가공은 Writer에서도 충분히 구현 가능하지만 객체지향의 핵심인 책임과 역할을 중심으로 보면 Processor, Writer를 분리하는 것이 좋습니다.

![](https://camo.githubusercontent.com/c5dd8fb6b96a268a1b2dd8cc8f985a35a27d0b7b/68747470733a2f2f646f63732e737072696e672e696f2f737072696e672d62617463682f646f63732f342e302e782f7265666572656e63652f68746d6c2f696d616765732f6368756e6b2d6f7269656e7465642d70726f63657373696e672e706e67)
> [이미지 출처 docs.spring.io](https://docs.spring.io/spring-batch/docs/4.0.x/reference/html/index-single.html#chunkOrientedProcessing)

ItemProcessor로 크게 2가지 처리를 합니다.

* 반환 
  * Reader에서 읽은 데이터를 원하는 데이터 타입으로 변환해서 Writer에게 넘긴다.
* 필터
  * Reader에서 넘겨준 데이터를 Writer로 넘겨 줄것인지를 결정
  * **null을 반환하면 Writer에 전달되지 않습니다.**

## 기본 사용법
```java
public interface ItemProcessor<I, O> {

  O process(I item) throws Exception;

}
```

* I : ItemReader에서 받은 데이터 타입
* O : ItemWriter에 보낼 데이터 타입

`Reader`에서 읽은 데이터가 `ItemProcessor`의 `processor` 를 통과해서 `Writer`에 전달됩니다.


```kotlin
@Bean
fun jpaItemProcessor(): ItemProcessor<Order, Order2> {
    return ItemProcessor { order: Order -> Order2(order.amount) }
}
```
익명 클래스 혹은 람다식을 사용하면 불필요한 코드가 없이 구현 코드 양이 적어 빠르게 구현이 가능합니다. 고정된 형태가 없어서 원하는 형태의 어떤 처리도 가능합니다.

## 변환
변환이란 Reader에서 읽은 타입을 변환하여 Writer에 전달해주는 것을 의미합니다.

```kotlin
@Configuration
class ProcessorConvertJobConfiguration(
        private val jobBuilderFactory: JobBuilderFactory,
        private val stepBuilderFactory: StepBuilderFactory,
        private val entityManagerFactory: EntityManagerFactory
) {

    private val chunkSize = 100

    private val log by logger()

    @Bean
    fun processorConvertJob(): Job {
        return jobBuilderFactory
                .get("processorConvertJob")
                .incrementer(RunIdIncrementer())
                .start(processorConvertStep())
                .build()
    }

    @Bean
    @JobScope
    fun processorConvertStep(): Step {
        return stepBuilderFactory
                .get("processorConvertStep")
                .chunk<Order, String>(chunkSize)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build()
    }

    @Bean
    fun reader(): JpaPagingItemReader<Order> {
        return JpaPagingItemReaderBuilder<Order>()
                .name("reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("select o from Order o")
                .build()
    }

    @Bean
    fun processor(): ItemProcessor<Order, String> {
        return ItemProcessor {
            it.amount.toString()
        }
    }

    fun writer(): ItemWriter<String> {
        return ItemWriter { items ->
            for (item in items) {
                log.info("amount value = {}", item)
            }
        }
    }
}
```

ItemProcessor에서는 Reader에서 읽어올 타입이 `Order`이며, Writer에서 넘겨줄 타입이 `String` 이기 때문에 제네릭 타입은 `<Teacher, String>`가 됩니다. 즉 `<Input, Output>`의 타입이 되는 것입니다.


```kotlin
@Bean
fun processor(): ItemProcessor<Order, String> {
    return ItemProcessor {
        it.amount.toString()
    }
}
```
여기서 **ChunkSize 앞에 선언될 타입 역시 Reader와 Writer 타입을 따라가야하기 때문에 다음과 같이 선언됩니다.**

## 필터
Writer에 값을 넘길지 말지를 Processor에서 판단하는 것을 판단 하는 필터의 역할을 합니다. `Order`의 amount가 짝수인 것을 필터링 하는 예제입니다.

```kotlin
@Configuration
class ProcessorFilterJobConfiguration(
        private val jobBuilderFactory: JobBuilderFactory,
        private val stepBuilderFactory: StepBuilderFactory,
        private val entityManagerFactory: EntityManagerFactory
) {
    private val chunkSize = 10;
    private val log by logger()

    @Bean
    fun processorFilterJob(): Job {
        return jobBuilderFactory.get("processorFilterJob")
                .incrementer(RunIdIncrementer())
                .start(processorFilterStep())
                .build()
    }

    @Bean
    @JobScope
    fun processorFilterStep(): Step {
        return stepBuilderFactory.get("step")
                .chunk<Order, Order>(chunkSize)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build()
    }

    @Bean
    fun reader(): JpaPagingItemReader<Order> {
        return JpaPagingItemReaderBuilder<Order>()
                .name("reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("select o from Order o")
                .build()
    }

    @Bean
    fun processor(): ItemProcessor<Order, Order> {
        return ItemProcessor label@{
            val amount = it.amount
            if (BigDecimal.ZERO == amount.divide(BigDecimal(2))) {
                return@label null
            }
            it
        }
    }

    private fun writer(): ItemWriter<Order> {
        return ItemWriter {
            for (order in it)
                log.info("amount value :  ${order.amount}")
        }
    }
}
```
```
2020-01-24 04:42:04.439 DEBUG 25963 --- [           main] o.s.batch.repeat.support.RepeatTemplate  : Repeat operation about to start at count=2
2020-01-24 04:42:04.439 DEBUG 25963 --- [           main] o.s.batch.repeat.support.RepeatTemplate  : Repeat operation about to start at count=3
2020-01-24 04:42:04.439 DEBUG 25963 --- [           main] o.s.batch.repeat.support.RepeatTemplate  : Repeat operation about to start at count=4
2020-01-24 04:42:04.439 DEBUG 25963 --- [           main] o.s.batch.repeat.support.RepeatTemplate  : Repeat operation about to start at count=5
2020-01-24 04:42:04.439 DEBUG 25963 --- [           main] o.s.batch.repeat.support.RepeatTemplate  : Repeat operation about to start at count=6
2020-01-24 04:42:04.439 DEBUG 25963 --- [           main] o.s.batch.repeat.support.RepeatTemplate  : Repeat operation about to start at count=7
2020-01-24 04:42:04.439 DEBUG 25963 --- [           main] o.s.batch.repeat.support.RepeatTemplate  : Repeat operation about to start at count=8
2020-01-24 04:42:04.439 DEBUG 25963 --- [           main] o.s.batch.repeat.support.RepeatTemplate  : Repeat operation about to start at count=9
2020-01-24 04:42:04.439 DEBUG 25963 --- [           main] o.s.batch.repeat.support.RepeatTemplate  : Repeat operation about to start at count=10
2020-01-24 04:42:04.439 DEBUG 25963 --- [           main] o.s.batch.repeat.support.RepeatTemplate  : Repeat is complete according to policy and result value.
2020-01-24 04:42:04.439  INFO 25963 --- [           main] uration$$EnhancerBySpringCGLIB$$4e540223 : amount value :  2648.00
2020-01-24 04:42:04.439  INFO 25963 --- [           main] uration$$EnhancerBySpringCGLIB$$4e540223 : amount value :  6320.00
2020-01-24 04:42:04.439  INFO 25963 --- [           main] uration$$EnhancerBySpringCGLIB$$4e540223 : amount value :  3658.00
2020-01-24 04:42:04.439  INFO 25963 --- [           main] uration$$EnhancerBySpringCGLIB$$4e540223 : amount value :  9329.00
2020-01-24 04:42:04.439  INFO 25963 --- [           main] uration$$EnhancerBySpringCGLIB$$4e540223 : amount value :  5670.00
2020-01-24 04:42:04.439  INFO 25963 --- [           main] uration$$EnhancerBySpringCGLIB$$4e540223 : amount value :  362.00
2020-01-24 04:42:04.439  INFO 25963 --- [           main] uration$$EnhancerBySpringCGLIB$$4e540223 : amount value :  4802.00
2020-01-24 04:42:04.439  INFO 25963 --- [           main] uration$$EnhancerBySpringCGLIB$$4e540223 : amount value :  2924.00
2020-01-24 04:42:04.439  INFO 25963 --- [           main] uration$$EnhancerBySpringCGLIB$$4e540223 : amount value :  213.00
2020-01-24 04:42:04.439  INFO 25963 --- [           main] uration$$EnhancerBySpringCGLIB$$4e540223 : amount value :  2295.00
```

`ItemProcessor`에서는 Order Amount 짝수일 경우는 `return null`을 함으로써 Writer에 넘기지 않도록 합니다. 코틀린에서는 `return null` 하기 위해서는  `label@` 키워드를 추가 해야합니다.

## 트랜잭션 범위
Spring Batch에서 **트랜잭션 범위는 Chunk단위 입니다.**  Reader에서 Entity를 반환해주었다면 **Entity간의 Lazy Loading이 가능합니다** 이는 Processor, Writer 모두 가능합니다.

### Processor

**Processor에서의 Lazy Loading 입니다.**

```kotlin
@Configuration
class ProcessorTransactionJobConfiguration(
        private val jobBuilderFactory: JobBuilderFactory,
        private val stepBuilderFactory: StepBuilderFactory,
        private val entityManagerFactory: EntityManagerFactory
) {

    private val chunkSize = 20
    private val log by logger()

    @Bean
    fun processorTransactionJob(): Job {
        return jobBuilderFactory.get("processorTransactionJob")
                .incrementer(RunIdIncrementer())
                .start(step())
                .build()
    }

    @Bean
    fun step(): Step {
        return stepBuilderFactory.get("step")
                .chunk<Order, Order>(chunkSize)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build()
    }

    fun reader(): JpaPagingItemReader<Order> {
        return JpaPagingItemReaderBuilder<Order>()
                .name("reader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select o from Order o")
                .pageSize(chunkSize)
                .build()
    }

    fun processor(): ItemProcessor<Order, Order> {
        return ItemProcessor {
            log.info("Item Processor order item size  ----------> ${it.items.size}")
            it
        }
    }

    fun writer(): ItemWriter<Order> {
        return ItemWriter {
            for (order in it) {
                log.info("ItemWriter Processor ----------> ${order.amount}")
            }
        }
    }
}
```

`log.info("Item Processor order item size  ----------> ${it.items.size}")` 코드에서 Lazy Loading을 진행합니다. 아래 로그에서 확인 할 수 있습니다.

```
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.940  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 1
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.942  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 3
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.943  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 3
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.945  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 1
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.946  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 3
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.947  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 0
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.949  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 3
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.950  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 2
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.952  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 1
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.953  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 2
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.955  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 2
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.956  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 2
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.958  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 3
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.959  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 3
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.961  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 4
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.962  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 2
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.964  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 2
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.965  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 2
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.967  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 1
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:49:59.969  INFO 32143 --- [           main] uration$$EnhancerBySpringCGLIB$$f3763f9d : Item Processor order item size  ----------> 3
```

### Writer

```
@Bean
fun step(): Step {
    return stepBuilderFactory.get("step")
            .chunk<Order, Order>(chunkSize)
            .reader(reader())
            .writer(writer())
            .build()
}

fun reader(): JpaPagingItemReader<Order> {
    return JpaPagingItemReaderBuilder<Order>()
            .name("reader")
            .entityManagerFactory(entityManagerFactory)
            .queryString("select o from Order o")
            .pageSize(chunkSize)
            .build()
}


fun writer(): ItemWriter<Order> {
    return ItemWriter {
        for (order in it) {
            log.info("Item Writer Processor ----------> ${order.items.size}")
        }
    }
}
```
위 코드는 Processor 코드를 제거하여 read -> wirte으로 진행하며 `log.info("Item Writer Processor ----------> ${order.items.size}")` Lazy Loading을 진행합니다.

```
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:55:33.111  INFO 32330 --- [           main] uration$$EnhancerBySpringCGLIB$$3c9b8840 : Item Writer Processor ----------> 0
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:55:33.113  INFO 32330 --- [           main] uration$$EnhancerBySpringCGLIB$$3c9b8840 : Item Writer Processor ----------> 0
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:55:33.115  INFO 32330 --- [           main] uration$$EnhancerBySpringCGLIB$$3c9b8840 : Item Writer Processor ----------> 0
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:55:33.116  INFO 32330 --- [           main] uration$$EnhancerBySpringCGLIB$$3c9b8840 : Item Writer Processor ----------> 0
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:55:33.118  INFO 32330 --- [           main] uration$$EnhancerBySpringCGLIB$$3c9b8840 : Item Writer Processor ----------> 0
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:55:33.119  INFO 32330 --- [           main] uration$$EnhancerBySpringCGLIB$$3c9b8840 : Item Writer Processor ----------> 0
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:55:33.121  INFO 32330 --- [           main] uration$$EnhancerBySpringCGLIB$$3c9b8840 : Item Writer Processor ----------> 0
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:55:33.122  INFO 32330 --- [           main] uration$$EnhancerBySpringCGLIB$$3c9b8840 : Item Writer Processor ----------> 0
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:55:33.123  INFO 32330 --- [           main] uration$$EnhancerBySpringCGLIB$$3c9b8840 : Item Writer Processor ----------> 0
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:55:33.125  INFO 32330 --- [           main] uration$$EnhancerBySpringCGLIB$$3c9b8840 : Item Writer Processor ----------> 0
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:55:33.126  INFO 32330 --- [           main] uration$$EnhancerBySpringCGLIB$$3c9b8840 : Item Writer Processor ----------> 0
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:55:33.128  INFO 32330 --- [           main] uration$$EnhancerBySpringCGLIB$$3c9b8840 : Item Writer Processor ----------> 0
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:55:33.129  INFO 32330 --- [           main] uration$$EnhancerBySpringCGLIB$$3c9b8840 : Item Writer Processor ----------> 0
Hibernate: select items0_.order_id as order_id5_0_0_, items0_.id as id1_0_0_, items0_.id as id1_0_1_, items0_.created_at as created_2_0_1_, items0_.updated_at as updated_3_0_1_, items0_.item_code as item_cod4_0_1_, items0_.order_id as order_id5_0_1_ from order_item items0_ where items0_.order_id=?
2020-01-24 15:55:33.131  INFO 32330 --- [           main] uration$$EnhancerBySpringCGLIB$$3c9b8840 : Item Writer Processor ----------> 0
2020-01-24 15:55:33.131 DEBUG 32330 --- [           main] o.s.b.c.step.item.ChunkOrientedTasklet   : Inputs not busy, ended: true
2020-01-24 15:55:33.131 DEBUG 32330 --- [           main] o.s.batch.core.step.tasklet.TaskletStep  : Applying contribution: [StepContribution: read=14, written=14, filtered=0, readSkips=0, writeSkips=0, processSkips=0, exitStatus=EXECUTING]
2020-01-24 15:55:33.132 DEBUG 32330 --- [           main] o.s.batch.core.step.tasklet.TaskletStep  : Saving step execution before commit: StepExecution: id=57, version=98, name=step, status=STARTED, exitStatus=EXECUTING, readCount=1954, filterCount=0, writeCount=1954 readSkipCount=0, writeSkipCount=0, processSkipCount=0, commitCount=98, rollbackCount=0, exitDescription=
2020-01-24 15:55:33.138 DEBUG 32330 --- [           main] o.s.batch.repeat.support.RepeatTemplate  : Repeat is complete according to policy and result value.
2020-01-24 15:55:33.138 DEBUG 32330 --- [           main] o.s.batch.core.step.AbstractStep         : Step execution success: id=57
2020-01-24 15:55:33.150 DEBUG 32330 --- [           main] o.s.batch.core.step.AbstractStep         : Step execution complete: StepExecution: id=57, version=100, name=step, status=COMPLETED, exitStatus=COMPLETED, readCount=1954, filterCount=0, writeCount=1954 readSkipCount=0, writeSkipCount=0, processSkipCount=0, commitCount=98, rollbackCount=0
2020-01-24 15:55:33.155 DEBUG 32330 --- [           main] o.s.batch.core.job.AbstractJob           : Upgrading JobExecution status: StepExecution: id=57, version=100, name=step, status=COMPLETED, exitStatus=COMPLETED, readCount=1954, filterCount=0, writeCount=1954 readSkipCount=0, writeSkipCount=0, processSkipCount=0, commitCount=98, rollbackCount=0, exitDescription=
2020-01-24 15:55:33.155 DEBUG 32330 --- [           main] o.s.batch.core.job.AbstractJob           : Job execution complete: JobExecution: id=47, version=1, startTime=Fri Jan 24 15:55:28 KST 2020, endTime=null, lastUpdated=Fri Jan 24 15:55:28 KST 2020, status=COMPLETED, exitStatus=exitCode=COMPLETED;exitDescription=, job=[JobInstance: id=44, version=0, Job=[processorTransactionJob]], jobParameters=[{run.id=12, targetAmount=100, version=15, -job.name=processorTransactionJob}]
2020-01-24 15:55:33.164  INFO 32330 --- [           main] o.s.b.c.l.support.SimpleJobLauncher      : Job: [SimpleJob: [name=processorTransactionJob]] completed with the following parameters: [{run.id=12, targetAmount=100, version=15, -job.name=processorTransactionJob}] and the following status: [COMPLETED]
2020-01-24 15:55:33.164 DEBUG 32330 --- [           main] BatchConfiguration$ReferenceTargetSource : Initializing lazy target object
2020-01-24 15:55:33.166  WARN 32330 --- [       Thread-7] o.s.b.f.support.DisposableBeanAdapter    : Destroy method 'close' on bean with name 'reader' threw an exception: org.springframework.batch.item.ItemStreamException: Error while closing item reader
2020-01-24 15:55:33.166  INFO 32330 --- [       Thread-7] o.s.s.concurrent.ThreadPoolTaskExecutor  : Shutting down ExecutorService 'applicationTaskExecutor'
2020-01-24 15:55:33.166  WARN 32330 --- [       Thread-7] o.s.b.f.support.DisposableBeanAdapter    : Destroy method 'close' on bean with name 'jpaItemWriterReader' threw an exception: org.springframework.batch.item.ItemStreamException: Error while closing item reader
2020-01-24 15:55:33.166  INFO 32330 --- [       Thread-7] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2020-01-24 15:55:33.167  INFO 32330 --- [       Thread-7] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2020-01-24 15:55:33.170  INFO 32330 --- [       Thread-7] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.

Process finished with exit code 0

```