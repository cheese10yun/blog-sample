> [Spring Batch 가이드 - 5. Spring Batch Scope & Job Parameter](https://jojoldu.tistory.com/455)을 보고 정리한 글입니다.


# Spring Batch Scope & Job Parameter

Spring Batch 경우 외부 혹은 내부에서 파라미터를 받아 여러 Batch 컴포넌트에서 사용할 수있게 지원하고 있습니다. **Job Parameter를 사용하기 위해선 항상 Spring Batch 전용 Scope를 선언해야 합니다.**

Spring Batch Scope는 `@StepScope`, `@JobScope` 2가지가 있습니다. 사용법은
```java
@Value("#{jobParameters[파라미터명]}")
```

> jobParameters 외에도 `jobExecutionContext`, `stepExecutionContext` 등도 SpEL로 사용할 수 있습니다. `@JobScop`에서는 `stepExecutionContext는` 사용할 수없고, `jobParameters`와 `jobExecutionContext`만 사용할 수 있습니다.

```kotlin
@Bean
fun orderDailySumJob(): Job {
    return jobBuilderFactory.get("orderDailySumJob")
            .incrementer(RunIdIncrementer())
            .start(orderDailySumStep(""))
            .build()
}

@Bean
@JobScope
fun orderDailySumStep(@Value("#{jobParameters[requestDate]}") requestDate: String): Step {
    log.info("requestDate: $requestDate")
    return stepBuilderFactory.get("orderDailrequestDateySumStep")
            .tasklet(tasklet(""))
            .build()
}

@Bean
@StepScope
fun tasklet(@Value("#{jobParameters[requestDate]}") requestDate: String): (contribution: StepContribution, chunkContext: ChunkContext) -> RepeatStatus? {
    log.info("requestDate: $requestDate")
    return { stepContribution, chunkContext ->
        run {
            val orders = orderRepository.findAll()
            var sumAmount: BigDecimal = BigDecimal.ZERO
            for (order in orders) {
                sumAmount = sumAmount.plus(order.amount)
            }
            println(sumAmount)
        }
        RepeatStatus.FINISHED
    }
}
```

**`@JobScope`는 Step 선언문에서 사용 가능하고, `@StepScoe`는 Tasklet이나 ItemReader, ItemWriter, ItemProcessor에서 사용 할 수 있습니다.**

현재 Job Parameter의 타입으로 사용할 수 있는 것은 Double, Long, Date, String 이 있습니다. LocalDate, LocalDateTime은 지원하지 않아 String 타입으로 받고 변환 작업을 진행 해야합니다.

해당 코드를 보면 

```kotlin
.start(orderDailySumStep(""))
.tasklet(tasklet("")) 
```
해당 Step, Tasklet에서 requestDate를 받지만 빈문자열로 보내주고 있습니다. 이는 Job Parameter의 할당이 어플리케이션 실행시에 하지 않기 때문에 가능합니다. 

## @StepScoe, @JobScope 소개
Spring Batch는 @StepScope와 @JobScope 일반적인 Spring Bean 사이클과 다른 부분이 있습니다.

```kotlin
@Bean
@JobScope
fun orderDailySumStep(@Value("#{jobParameters[requestDate]}") requestDate: String): Step {
    log.info("requestDate: $requestDate")
    return stepBuilderFactory.get("orderDailrequestDateySumStep")
            .tasklet(tasklet(""))
            .build()
}
```
Spring Batch가 Spring 컨테이너를 통해 지정된 **Step의 실행시점에 해당 컨포넌트를 Spring Bean으로 생성합니다.** 마찬가지로 @JobScope는 Job 실행시점에 Bean이 생성됩니다. 즉 **Bean의 생성 시점을 지정된 Scope가 실행되는 시점으로 지연시킵니다.**

> 어떻게 보면 MVC의 request scope와 비슷할 수 있겠습니다.
request scope가 request가 왔을때 생성되고, response를 반환하면 삭제되는것처럼, JobScope, StepScope 역시 Job이 실행되고 끝날때, Step이 실행되고 끝날때 생성/삭제가 이루어진다고 보시면 됩니다.

이렇게 Bean의 생성시점을 어플리케이션 실행 시점이 아닌, Step 혹은 Job의 실행시점으로 지연시키면서 얻는 장점은 크게 2가지가 있습니다.

### jobParameters의 Late Binding이 가능
Job Parameter가 StepContext 또는 JobExecutionContext 레벨에서 할당시킬 수 있습니다. **반드시 Application이 실행되는 시점이 아니더라도 service 단계에서 Job Parameter를 할당 시킬 수 있습니다.**


### 동일한 컴포넌트를 병렬 혹은 동시에 사용할 때 유용
Step 안에 Tasklet이 있고, 이 Tasklet은 멤버 변수와 이 멤버 변수를 변경하는 로직이 있는 경우 `@StepScoe` 없이 Step을 병렬로 시키게되면 **서로 다른 Step에서 하나의 Tasklet를 두고 상태를 변경하려 합니다.** 

**하지만 `@StepScoe`가 있다면 각각의 Stepㅇ에서 별도의 Tasklet을 생성하고 관리하기 대문에 서로의 상태를 침범할 일이 없습니다.** (Bean의 생성 시점이 Scope가 실행되는 시점임으로 Late init이 되고 각각의 Step에서 별도의 Tasklet을 생성 하기 때문)


## Job Parameter 오해
Job Parameters는 @Value를 통해서 가능합니다. Job Parameters는 Step이나, Tasklet, Reader 등 Batch 컴포넌트 Bean의 생성 시점에 호출할 수 있습니다만, **정확히는 Scope Bean을 생성할때만 가능합니다.**

```kotlin
@Bean
fun orderDailySumJob(): Job {
    return jobBuilderFactory.get("orderDailySumJob")
            .incrementer(RunIdIncrementer())
            .start(orderDailySumStep(""))
            .build()
}

@Bean
// @JobScope 주석
fun orderDailySumStep(@Value("#{jobParameters[requestDate]}") requestDate: String): Step {
    log.info("requestDate: $requestDate")
    return stepBuilderFactory.get("orderDailrequestDateySumStep")
            .tasklet(tasklet(""))
            .build()
}
```
`@JobScope` 주석 처리하고 해당 Job을 실행하면 아래와 같습니다.

```
2020-01-17 01:47:35.254  INFO 6030 --- [           main] o.h.t.schema.internal.SchemaCreatorImpl  : HHH000476: Executing import script 'org.hibernate.tool.schema.internal.exec.ScriptSourceInputNonExistentImpl@2152ab30'
2020-01-17 01:47:35.255  INFO 6030 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2020-01-17 01:47:35.662  INFO 6030 --- [           main] o.h.h.i.QueryTranslatorFactoryInitiator  : HHH000397: Using ASTQueryTranslatorFactory
2020-01-17 01:47:35.789  INFO 6030 --- [           main] ailySum$$EnhancerBySpringCGLIB$$468f8883 : requestDate: 
```
 `.tasklet(tasklet(""))` 에서 념겨준 빈문자열을 그대로 `requestDate: ` 찍히는 것을 확인 할 수 있습니다. 즉 JobParameters를 사용하기 위해선 꼭 @StepScope, @JobScope로 Bean을 생성해야 합니다.

 ## JobParameter vs 시스템 변수

 기존에 Spring Boot에서 사용하던 여러 환경변수 혹은 시스템 변수를 사용할 수 있습니다. CommandLineRunner를 사용한다면 `java jar application.jar -D파라미터`로 시스템변수를 지정해서 사용할 수 있씁니다. 하지만 왜 Job Parameter를 사용해야하는지 알아 보겠습니다.

 ### JobParameter
 ```java
@Bean
@StepScope
public FlatFileItemReader<Partner> reader(
        @Value("#{jobParameters[pathToFile]}") String pathToFile){
    FlatFileItemReader<Partner> itemReader = new FlatFileItemReader<Partner>();
    itemReader.setLineMapper(lineMapper());
    itemReader.setResource(new ClassPathResource(pathToFile));
    return itemReader;
}
 ```

 ### 시스템 변수

```java
@Bean
@ConfigurationProperties(prefix = "my.prefix")
protected class JobProperties {

    String pathToFile;

    ...getters/setters
}

@Autowired
private JobProperties jobProperties;

@Bean
public FlatFileItemReader<Partner> reader() {
    FlatFileItemReader<Partner> itemReader = new FlatFileItemReader<Partner>();
    itemReader.setLineMapper(lineMapper());
    String pathToFile = jobProperties.getPathToFile();
    itemReader.setResource(new ClassPathResource(pathToFile));
    return itemReader;
}
```

위 2가지 방식에는 몇 가지 차이점이 있습니다.

#### 시스템 변수를 사용할 경우 Spring Batch의 Job Parameter 관련 기능 사용 못한다.
예를 들어, Spring Batch는 같은 JobParameter로 같은 Job을 두 번 실행하지 않습니다.
하지만 시스템 변수를 사용하게 될 경우 이 기능이 전혀 작동하지 않습니다. 또 **Spring Batch에서 자동으로 관리해주는 Parameter 관련 메타 테이블이 전혀 관리되지 않습니다.**

#### Command line이 아닌 다른 방법으로 Job을 실행하기가 어렵다.
만약 실행해야한다면 전역 상태 (시스템 변수 혹은 환경 변수)를 동적으로 계속해서 변경시킬 수 있도록 Spring Batch를 구성해야합니다.
동시에 여러 Job을 실행하려는 경우 또는 테스트 코드로 Job을 실행해야할때 문제가 발생할 수 있습니다.

####  Late Binding을 사용하지 못한다.
Job Parameter를 못쓴다는 Late Binding을 못하게 된다는 의미입니다. 