# JPA Paging Reader 정리

> 해당 글은 [처음으로 배우는 스프링 부트 2](http://www.hanbit.co.kr/store/books/look.php?p_code=B4458049183), [기억보단 기록을 : Spring Batch Paging Reader 사용시 같은 조건의 데이터를 읽고 수정할때 문제](https://jojoldu.tistory.com/337)을 보고 학습한 내용을 정리한 글입니다.

# JobParameter

```kotlin
    @Bean(destroyMethod = "")
    @StepScope
    fun orderPagingReader(@Value("#{jobParameters[targetAmount]}") targetAmount: BigDecimal): JpaPagingItemReader<Order> {
        println("=============")
        println(targetAmount)
        println("=============")
        
        ...
        ...
        return itemReader
    }
```

SpEL을 이용해서 JobParameter에서 targetAmount를 전달받습니다.`@Value("#{jobParameters[targetAmount]}") targetAmount: BigDecimal` 코드입니다. 

```
--job.name=orderPagingJob -targetAmount=10.10 version=xx
```
위 처럼 `-targetAmount=10.10`으로 Job Parameter을 전달합니다.


