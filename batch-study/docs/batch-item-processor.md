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
Writer에 값을 넘길지 말지를 Processor에서 판단하는 것을 판단 하는 필터의 역할을 합니다.

