> [spring batch in action : 메타테이블엿보기](https://github.com/jojoldu/spring-batch-in-action/blob/master/3_%EB%A9%94%ED%83%80%ED%85%8C%EC%9D%B4%EB%B8%94%EC%97%BF%EB%B3%B4%EA%B8%B0.md)을 보고 정리한 글입니다.

# Job

## Simple Job

간단한 Simple Job을 구성 합니다. Step1, Step2으로 구성하고 Step1에서 무조건 실패하게 구성했습니다.

```kotlin
@Configuration
class SimpleJobConfiguration(
        private val jobBuilderFactory: JobBuilderFactory,
        private val stepBuilderFactory: StepBuilderFactory
) {
    
    private val log by logger()

    @Bean
    fun simpleJob(): Job {
        return jobBuilderFactory.get("simpleJob")
                .start(simpleStep1(null))
                .next(simpleStep2(null))
                .build()
    }

    @Bean
    @JobScope
    fun simpleStep1(@Value("#{jobParameters[targetDate]}") targetDate: String?): Step {
        return stepBuilderFactory.get("simpleStep1")
                .tasklet { contribution, chunkContext ->
                    log.info("This is Step 1")
                    log.info("targetDate = $targetDate")
                    throw IllegalArgumentException("asd")
                    // RepeatStatus.FINISHED
                }
                .build()
    }

    @Bean
    @JobScope
    fun simpleStep2(@Value("#{jobParameters[targetDate]}") targetDate: String?): Step {
        return stepBuilderFactory.get("simpleStep1")
                .tasklet { contribution, chunkContext ->
                    log.info("This is Step 2")
                    log.info("targetDate = $targetDate")
                    RepeatStatus.FINISHED
                }
                .build()
    }
}
```

## BATCH_JOB_EXECUTION

STEP_EXECUTION_ID | VERSION | STEP_NAME | JOB_EXECUTION_ID | START_TIME | END_TIME | STATUS | COMMIT_COUNT | READ_COUNT | FILTER_COUNT | WRITE_COUNT | READ_SKIP_COUNT | WRITE_SKIP_COUNT | PROCESS_SKIP_COUNT | ROLLBACK_COUNT | EXIT_CODE | EXIT_MESSAGE | LAST_UPDATED
------------------|---------|-----------|------------------|------------|----------|--------|--------------|------------|--------------|-------------|-----------------|------------------|--------------------|----------------|-----------|--------------|-------------
6 | 2 | simpleStep1 | 6 | 2020-01-14 16:35:24 | 2020-01-14 16:35:24 | FAILED | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | FAILED | java.lang.IllegalArgumentException: asd | 2020-01-14 16:35:24
7 | 2 | simpleStep1 | 7 | 2020-01-14 16:40:39 | 2020-01-14 16:40:39 | FAILED | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | FAILED | java.lang.IllegalArgumentException: asd | 2020-01-14 16:40:39

BATCH_JOB_EXECUTION 테이블을 보면 STATUS 칼럼이 FAILED인것을 확인 할 수 있습니다. 이제 다시 `throw IllegalArgumentException("asd")` 주석을 진행하고 정상적으로 Step1이 정상 동작하게
변경합니다.

STEP_EXECUTION_ID | VERSION | STEP_NAME | JOB_EXECUTION_ID | START_TIME | END_TIME | STATUS | COMMIT_COUNT | READ_COUNT | FILTER_COUNT | WRITE_COUNT | READ_SKIP_COUNT | WRITE_SKIP_COUNT | PROCESS_SKIP_COUNT | ROLLBACK_COUNT | EXIT_CODE | EXIT_MESSAGE | LAST_UPDATED
------------------|---------|-----------|------------------|------------|----------|--------|--------------|------------|--------------|-------------|-----------------|------------------|--------------------|----------------|-----------|--------------|-------------
8 | 3 | simpleStep1 | 8 | 2020-01-14 16:41:49 | 2020-01-14 16:41:49 | COMPLETED | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | COMPLETED |  | 2020-01-14 16:41:49
9 | 3 | simpleStep1 | 8 | 2020-01-14 16:41:49 | 2020-01-14 16:41:49 | COMPLETED | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | COMPLETED |  | 2020-01-14 16:41:49

동일한 Job Parameter로 2번 실행했는데 같은 파라미터로 실행되었다는 에러가 발생하지 않습니다. **즉 Spring Batch에서는 동일한 Job Parameter로 성공한 기록이 있을 경우에만 재실행 되지 않습니다.**

## Next

```kotlin
@Configuration
class SimpleJobConfiguration(
        private val jobBuilderFactory: JobBuilderFactory,
        private val stepBuilderFactory: StepBuilderFactory
) {
    private val log by logger()

    @Bean
    fun simpleJob(): Job {
        return jobBuilderFactory.get("simpleJob")
                .incrementer(RunIdIncrementer())
                .start(simpleStep1())
                .next(simpleStep2())
                .next(simpleStep3())
                .build()
    }

    @Bean
    fun simpleStep1(): Step {
        return stepBuilderFactory.get("simpleStep1")
                .tasklet { contribution, chunkContext ->
                    log.info("This is Step 1")
                    RepeatStatus.FINISHED
                }
                .build()
    }

    @Bean
    fun simpleStep2(): Step {
        return stepBuilderFactory.get("simpleStep1")
                .tasklet { contribution, chunkContext ->
                    log.info("This is Step 2")
                    RepeatStatus.FINISHED
                }
                .build()
    }

    @Bean
    fun simpleStep3(): Step {
        return stepBuilderFactory.get("simpleStep1")
                .tasklet { _, _ ->
                    log.info("This is Step 3")
                    RepeatStatus.FINISHED
                }
                .build()
    }
}
```

`next()` 순차적으로 Step들을 연결 시킬때 사용합니다. 해당 잡은 step1 -> step2 -> step3 으로 순차적으로 Job이 실행 됩니다.

## 지정된 Job만 실항

```yml
spring:
  batch.job.names: ${job.name:NONE}
```

Spring Batch가 실행될때 Program arguments로 `job.name` 값이 넘어오면 **해당 값과 일치하는 Job만 실행할 수 있게 합니다.** 해당 코드의 의미는 `job.name`이 있으면 `job.name`에 할당하고
없으면 `NONE`을 할당하라는 의미입니다. `job.names`가 `NONE`인 경우 어떠한 Job도 실행되지 않습니다. 실제 jar을 실행 시키는
운영환경에서는 `java -jar batch-application.jar --job.name=simpleJob` 으로 job.name을 지정하게 됩니다.

## 조건별 흐름제어 Flow

Next가 순차적으로 Step의 순서를 제어할 수는 있지만 Step에서 오류가 나면 나머지 뒤에 있는 Step 들은 실행되지 못한다는 문제가 있습니다. **필요에 따라 정상일때는 Step B로, 오류가 발생했을 경우에는 Step C로 Step을 조정할
필요가 있습니다.**

![](https://github.com/cheese10yun/TIL/raw/master/assets/batch-flow.png)

```kotlin
@Configuration
class StepNextConditionJobConfiguration(
        private val jobBuilderFactory: JobBuilderFactory,
        private val stepBuilderFactory: StepBuilderFactory
) {

    private val log by logger()

    @Bean
    fun stepNextConditionalJob(): Job {
        ////@formatter:off
        return jobBuilderFactory.get("stepNextConditionalJob")
                .start(conditionalJobStep1())
                    .on("FAILED") // FAILED 일 경우
                    .to(conditionalJobStep3()) // Step3 으로 이동한다.
                    .on("*") // Step3의 결과와 관계 없이
                    .end() // Step3 이동이후 Flow 종료
                .from(conditionalJobStep1()) // Step1 으로부터
                    .on("*") // FAILED 외에 모든 경우
                    .to(conditionalJobStep2()) // Step2로 이동한다.
                    .next(conditionalJobStep3()) // Step2가 정상 종료되면 Step3로 이동한다.
                .end() // Job 종료
                .build()
        //@formatter:on
    }

    @Bean
    fun conditionalJobStep1(): Step {

        return stepBuilderFactory.get("step1")
                .tasklet { contribution, chunkContext ->
                    log.info("This is Step conditionalJobStep1 Step1")
                    contribution.exitStatus = ExitStatus.FAILED
                    RepeatStatus.FINISHED
                }
                .build()
    }

    @Bean
    fun conditionalJobStep2(): Step {
        return stepBuilderFactory.get("step2")
                .tasklet { contribution, chunkContext ->
                    log.info("This is Step conditionalJobStep2 Step2")
                    RepeatStatus.FINISHED
                }
                .build()
    }

    @Bean
    fun conditionalJobStep3(): Step {
        return stepBuilderFactory.get("step3")
                .tasklet { contribution, chunkContext ->
                    log.info("This is Step conditionalJobStep3 Step3")
                    RepeatStatus.FINISHED
                }
                .build()
    }

}
```

해당 코드의 시나리오는 다음과 같습니다.

* Step1 실패시 : Step1 -> Step3
* Step1 성공시 : Step1 -> Step2 -> Step3

```kotlin
@Bean
fun stepNextConditionalJob(): Job {
    ////@formatter:off
    return jobBuilderFactory.get("stepNextConditionalJob")
            .start(conditionalJobStep1())
                .on("FAILED") // FAILED 일 경우
                .to(conditionalJobStep3()) // Step3 으로 이동한다.
                .on("*") // Step3의 결과와 관계 없이
                .end() // Step3 이동이후 Flow 종료
            .from(conditionalJobStep1()) // Step1 으로부터
                .on("*") // FAILED 외에 모든 경우
                .to(conditionalJobStep2()) // Step2로 이동한다.
                .next(conditionalJobStep3()) // Step2가 정상 종료되면 Step3로 이동한다.
            .end() // Job 종료
            .build()
    //@formatter:on
}
```

* `.on()`
    * 캐치할 ExitStatus 지정,
    * `*`일 경우 모든 ExitStatus가 지정된다.
* `to()`
    * 다음으로 이동할 Step 지정
* `from()`
    * 일종의 이벤트 리스너 역할
    * 상태값을 보고 일치하는 상태라면 `to()`에 포함된 Step을 호출한다.
    * **Step1의 이벤트 캐치가 FAILED로 되어있는 상태에서 추가로 이벤트 캐치히려면 from을 써야만함**
* `end()`
    * end FlowBuilder를 반환하는 end와 FlowBuilder를 종료하는 end 2개가 있음
    * `on(*)` 뒤에 있는 end는 FlowBuilder를 반환하는 end
    * `build()` 앞에있는 end는 FlowBuilder를 종료하는 end
    * FlowBuilder를 반환하는 end는 계속해서 `from`을 이어갈 수 있음

중요한 부분은 `on`이 캐치하는 **상태값이 BatchStatus가 아닌 ExistStatus라는 점입니다.** 그래서 분기를 처리를 위하 상태값 조정이 필요하다면 ExitStatus를 조정해야합니다.

```kotlin
@Bean
fun conditionalJobStep1(): Step {

    return stepBuilderFactory.get("step1")
            .tasklet { contribution, chunkContext ->
                log.info("This is Step conditionalJobStep1 Step1")
                contribution.exitStatus = ExitStatus.FAILED
                RepeatStatus.FINISHED
            }
            .build()
}
```

ExistStatus를 FAILED로 지정합니다. 해당 status를 보고 Flow가 진행됩니다.

### Step1 실패시 : Step1 -> Step3

STEP_EXECUTION_ID | VERSION | STEP_NAME | JOB_EXECUTION_ID | START_TIME | END_TIME | STATUS | COMMIT_COUNT | READ_COUNT | FILTER_COUNT | WRITE_COUNT | READ_SKIP_COUNT | WRITE_SKIP_COUNT | PROCESS_SKIP_COUNT | ROLLBACK_COUNT | EXIT_CODE | EXIT_MESSAGE | LAST_UPDATED
------------------|---------|-----------|------------------|------------|----------|--------|--------------|------------|--------------|-------------|-----------------|------------------|--------------------|----------------|-----------|--------------|-------------
15 | 3 | step1 | 11 | 2020-01-15 15:58:07 | 2020-01-15 15:58:07 | COMPLETED | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | FAILED |  | 2020-01-15 15:58:07
16 | 3 | step3 | 11 | 2020-01-15 15:58:07 | 2020-01-15 15:58:07 | COMPLETED | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | COMPLETED |  | 2020-01-15 15:58:07

해당 Job을 실행하게되면 `STEP_NAME` step1-> step3 으로 실행된것을 볼 수 있습니다. 즉 `Step1 실패시 : Step1 -> Step3` 시나리오대로 실행되는 되었습니다.

### Step2 성공시 : Step1 -> Step2 -> Step3

STEP_EXECUTION_ID | VERSION | STEP_NAME | JOB_EXECUTION_ID | START_TIME | END_TIME | STATUS | COMMIT_COUNT | READ_COUNT | FILTER_COUNT | WRITE_COUNT | READ_SKIP_COUNT | WRITE_SKIP_COUNT | PROCESS_SKIP_COUNT | ROLLBACK_COUNT | EXIT_CODE | EXIT_MESSAGE | LAST_UPDATED
------------------|---------|-----------|------------------|------------|----------|--------|--------------|------------|--------------|-------------|-----------------|------------------|--------------------|----------------|-----------|--------------|-------------
17 | 3 | step1 | 12 | 2020-01-15 16:18:23 | 2020-01-15 16:18:23 | COMPLETED | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | COMPLETED |  | 2020-01-15 16:18:23
18 | 3 | step2 | 12 | 2020-01-15 16:18:23 | 2020-01-15 16:18:23 | COMPLETED | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | COMPLETED |  | 2020-01-15 16:18:23
19 | 3 | step3 | 12 | 2020-01-15 16:18:23 | 2020-01-15 16:18:23 | COMPLETED | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | COMPLETED |  | 2020-01-15 16:18:23

`STEP_NAME`을 확인하면 성공 시라리오 `Step2 성공시 : Step1 -> Step2 -> Step3`이 실행된것을 확인 할 수 있습니다.

### Batch Status vs Exit Status

Flow을 설명할때 **BatchStatus와 ExitStatus의 차이를 아는 것이 중요합니다.** BatchStatus는 Job 또는 Step 의 실행 결과를 Spring에서 기록할 때 사용하는 Enum입니다.

```
.on("FAILED").to(stepB())
```

해당 코드는 `on` 메서드가 참조하는 것이 BatchStatus으로 생각할 수 있지만 **실제 Step의 ExitStatus을 참조합니다.** ExitStatus는 **Step의 실행 후 상태를 이야기합니다.**(ExitStatus는 Enum이
아닙니다.)

해당 코드의 의미는 exitCode가 FAILED로 끝나게되면 StepB로 가라는 뜻입니다. **Spring Batch는 기본적으로 ExitStatus의 exitCode는 Step의 BatchStatus와 같도록 설정이 되어 있습니다.**

만약 커스텀한 exitCode가 필요하게 된다면 아래처럼 처리해야 합니다.

```
.start(step1())
    .on("FAILED")
    .end()
.from(step1())
    .on("COMPLETED WITH SKIPS")
    .to(errorPrint1())
    .end()
.from(step1())
    .on("*")
    .to(step2())
    .end()
```

* step이 실패하면 Job 실패
* step이 성공하면 step2가 수행
* step이 성공적으로 완려되며. `COMPLETED WITH SKIPS`의 exit 코드로 종료

## Decide

Step의 결과에 따라 서로 다른 Step으로 이동하는 방법을 알아보았습니다. 이번 에는 다른 방식으로 분기 처리하는 방식입니다. 위에서 진행했던 방식에 2가지 문제가 있습니다.

* Step이 담당하는 역할이 2개 이상이 됩니다.
    * 실제 해당 Step이 처리해야할 로직외에도 분기를 시키기 위해 ExitStatus 조작이 필요합니다.
* 다양한 분기 로직 처리의 어려움
    * ExitStatus를 커스텀하게 고치기 위해서는 Listener를 생성하고 Job Flow에 등록하는 등 번거로움이 존재합니다.

**Spring Batch에서는 Step들의 Flow속에서 분기만 담당하는 타입이 있습니다.**

```kotlin
@Configuration
class DeciderJobConfiguration(
        private val jobBuilderFactory: JobBuilderFactory,
        private val stepBuilderFactory: StepBuilderFactory
) {

    private val log by logger()

    @Bean
    fun deciderJob(inactiveJobListener: InactiveJobListener): Job {

        //@formatter:off
        return jobBuilderFactory.get("deciderJob")
                .listener(inactiveJobListener)
                .start(startStep())
                .next(decider()) // 짝수 or 홀 수 구분
                .from(decider()) // decider의 상태가
                    .on("ODD") // ODD 라면
                    .to(oddStep()) // oddStep 으로 간다
                .from(decider()) // decider 상태가
                    .on("EVEN") // EVEN 이면
                    .to(evenStep()) // evenStep 으로 간다
                .end()
                .build()
        //@formatter:on

    }

    @Bean
    fun startStep(): Step = stepBuilderFactory.get("startStep")
            .tasklet { contribution, chunkContext ->
                log.info("Start")
                RepeatStatus.FINISHED
            }
            .build()

    @Bean
    fun evenStep(): Step = stepBuilderFactory.get("evenStep")
            .tasklet { contribution, chunkContext ->
                log.info("짝수입니다.")
                RepeatStatus.FINISHED
            }
            .build()

    @Bean
    fun oddStep(): Step = stepBuilderFactory.get("oddStep")
            .tasklet { contribution, chunkContext ->
                log.info("홀수입니다.")
                RepeatStatus.FINISHED
            }
            .build()

    @Bean
    fun decider(): JobExecutionDecider = OddDecider()
}

class OddDecider : JobExecutionDecider {

    override fun decide(jobExecution: JobExecution, @Nullable stepExecution: StepExecution?): FlowExecutionStatus {
        val random = Random()
        val randomNumber = random.nextInt(50) + 1
        return when {
            randomNumber % 2 == 0 -> FlowExecutionStatus("EVEN")
            else -> FlowExecutionStatus("ODD")
        }
    }
}
```

* start()
    * Job Flow의 첫번째 Step을 시작합니다.
* next()
    * startStep 이후에 decider를 실행합니다.
* from()
    * from은 이벤트 리스너 역할을 합니다.
    * decider의 상태값을 보고 일치하는 상태라면 to()에 포함된 step 를 호출합니다.

분기 로직에 대한 모든 일은 `OddDecider`에서 전담하고 있습니다. 즉 분기에 대한 책임을 해당 객체에서 수행하고 Step 에서는 분기에 따른 책임을 가지게되지 않습니다.

STEP_EXECUTION_ID | VERSION | STEP_NAME | JOB_EXECUTION_ID | START_TIME | END_TIME | STATUS | COMMIT_COUNT | READ_COUNT | FILTER_COUNT | WRITE_COUNT | READ_SKIP_COUNT | WRITE_SKIP_COUNT | PROCESS_SKIP_COUNT | ROLLBACK_COUNT | EXIT_CODE | EXIT_MESSAGE | LAST_UPDATED
------------------|---------|-----------|------------------|------------|----------|--------|--------------|------------|--------------|-------------|-----------------|------------------|--------------------|----------------|-----------|--------------|-------------
20 | 3 | startStep | 13 | 2020-01-15 16:56:22 | 2020-01-15 16:56:22 | COMPLETED | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | COMPLETED |  | 2020-01-15 16:56:22
21 | 3 | evenStep | 13 | 2020-01-15 16:56:22 | 2020-01-15 16:56:22 | COMPLETED | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | COMPLETED |  | 2020-01-15 16:56:22
22 | 3 | startStep | 14 | 2020-01-15 16:57:39 | 2020-01-15 16:57:39 | COMPLETED | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | COMPLETED |  | 2020-01-15 16:57:39
23 | 3 | oddStep | 14 | 2020-01-15 16:57:39 | 2020-01-15 16:57:39 | COMPLETED | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | COMPLETED |  | 2020-01-15 16:57:39

evenStep, oddStep 홀수 짝수 Step들이 각각 실행된것을 확인 할 수 있씁니다.

 ```kotlin
 class OddDecider : JobExecutionDecider {

    override fun decide(jobExecution: JobExecution, @Nullable stepExecution: StepExecution?): FlowExecutionStatus {
        val random = Random()
        val randomNumber = random.nextInt(50) + 1
        return when {
            randomNumber % 2 == 0 -> FlowExecutionStatus("EVEN")
            else -> FlowExecutionStatus("ODD")
        }
    }
}
 ```

JobExecutionDecider 인터페이스를 구현한 OddDecider입니다. **주의하실 것은 Step으로 처리하는게 아니기 때문에 ExitStatus가 아닌 FlowExecutionStatus로 상태를 관리합니다.**

### 간단하게 Flow 보기

Step의 가장 기본적인 흐름은 `읽기-처리-쓰기` 입니다. 여기서 세부적인 조건에 따라서 Step의 실행 여부를 정할 수 있습니다. 이런 흐름은 제어하는 것이 Flow입니다.

![](https://github.com/cheese10yun/TIL/raw/master/assets/batch-flow.png)

위 흐름에서 조건에 해당하는 부분을 JobExecutionDecider 인터페이스를 사용해 구현할 수 있습니다. JobExecutionDecider 인터페이스는 `decide()` 메서드 하나만 제공합니다.

```java
public interface JobExecutionDecider {
	FlowExecutionStatus decide(JobExecution jobExecution, @Nullable StepExecution stepExecution);
}
```

`decide()` 메서드의 반환값으로 FlowExecutionStatus 객체를 반환하도록 명시되어 있습니다. FlowExecutionStatus 객체는 Statu의 값 `COMPLETED`, `STOPPED`, `FAILED`. `UNKOWN`
등을 제공합니다.

```kotlin
class InactiveJobExecutionDecider : JobExecutionDecider {

    override fun decide(jobExecution: JobExecution, stepExecution: StepExecution?): FlowExecutionStatus {
        if (Random().nextInt() > 0) {  // (1)
            println("FlowExecutionStatus.COMPLETED")
            return FlowExecutionStatus.COMPLETED // (2)
        }

        println("FlowExecutionStatus.FAILED")
        return FlowExecutionStatus.FAILED // (3)
    }
}
```

* (1) Random 객체를 사용해 정수 여부에 따라 분기 처리
* (2) 양수면 FlowExecutionStatus.COMPLETED 리턴
* (3) 음수면 FlowExecutionStatus.FAILED 리턴

```kotlin

@Suppress("SpringElInspection")
@Configuration
class OrderPaging(
        ....
        private val entityManagerFactory: EntityManagerFactory
) {

    private val CHUNK_SIZE: Int = 5

    @Bean
    fun orderPagingJob(orderPagingStep: Step, inactiveJobListener: InactiveJobListener, orderPagingJobFlow: Flow): Job {
        return jobBuilderFactory.get("orderPagingJob")
                .listener(inactiveJobListener)
                .start(orderPagingJobFlow) // (5)
                .end()
                .build()
    }

    @Bean
    fun orderPagingJobFlow(orderPagingStep: Step): Flow {
        val flowBuilder = FlowBuilder<Flow>("orderPagingJobFlow") // (1)
        return flowBuilder
                .start(InactiveJobExecutionDecider()) // (2)
                .on(FlowExecutionStatus.FAILED.name).end() // (3)
                .on(FlowExecutionStatus.COMPLETED.name).to(orderPagingStep) // (4)
                .end()
    }
}
```

* (1) FlowBuilder를 통해서 Flow 객체 생성, 생성자의 Flow 이름을 String으로 넘길수 있습니다.
* (2) InactiveJobExecutionDecider 객체를 star를 진행합니다.
* (3) `FlowExecutionStatus.FAILED` 인 경우에는 바로 종료하게 `end()` 메서드를 호출하게 합니다.
* (4) `FlowExecutionStatus.COMPLETED` 인 경우에는 기존 Job을 실행 하도록 `to()` 메서드를 호출하게 합니다.
* (5) 기존 Job에서 새로운 `orderPagingJobFlow`을 DI 받아 `start()` 합니다.

배치 흐름에서 전후 처리를 하는 Listener를 설정할 수 있습니다. 구체적으로 Job의 전후 처리, Step의 전후 처리, 각 청크 단위에서의 전후 처리 등 세세한 과정 실행 시 특정 로직을 할당해 제어할 수 있습니다.

## Batch Listener

인터페이스명               | 어노테이션                                                    | 설명
 ---------------------|----------------------------------------------------------|------------------------------------------------
JobExecutionListener | @BeforeJob  <br/> @AfterJob                              | Job 실행 전후 처리를 담당하는 Listener 설정
ChunkListener        | @BeforeChunk  <br/> @AfterChunk <br/> @AfterChunkError   | Chunk 실행 전후 처리 및 에러 발생 시 처리를 담당하는 Listener 설정
ItemReaderListener   | @BeforeRead <br/> @AfterRead <br/> @OnReadError          | Read 과정 전후 처리 및 에러 발생 시 처리를 담당하는 Listener 설정
ItemProcessListener  | @BeforeProcess <br/> @AfterProcess <br/> @OnProcessError | Process 과정 전후 처리 및 에러 발생 시 처리를 담당하는 Listener 설정
ItemWriterListener   | @BeforeWrite <br/> @AfterWrite <br/> @AOnWriterError     | Write 과정 전후 처리 및 에러 발생 시 처리를 담당하는 Listener 설정

## JobExecutionListener

 ```kotlin
 @Component
 class InactiveJobListener : JobExecutionListener {
 
     override fun beforeJob(jobExecution: JobExecution) {
         println("beforeJob") // beforeJob
         println(jobExecution.toString()) //JobExecution: id=203, version=1, startTime=Mon Sep 02 01:55:06 KST 2019, endTime=null, lastUpdated=Mon Sep 02 01:55:06 KST 2019, status=STARTED, exitStatus=exitCode=UNKNOWN;exitDescription=, job=[JobInstance: id=195, version=0, Job=[orderPagingJob]], jobParameters=[{version=5, -job.name=orderPagingJob, targetAmount=1209.10}]
     }
 
     override fun afterJob(jobExecution: JobExecution) {
         println("afterJob") // afterJob
         println(jobExecution.toString()) // JobExecution: id=203, version=1, startTime=Mon Sep 02 01:55:06 KST 2019, endTime=Mon Sep 02 01:55:06 KST 2019, lastUpdated=Mon Sep 02 01:55:06 KST 2019, status=COMPLETED, exitStatus=exitCode=COMPLETED;exitDescription=, job=[JobInstance: id=195, version=0, Job=[orderPagingJob]], jobParameters=[{version=5, -job.name=orderPagingJob, targetAmount=1209.10}]
     }
 }
 
 @Bean
 fun orderPagingJob(orderPagingStep: Step, inactiveJobListener: InactiveJobListener): Job {
     return jobBuilderFactory.get("orderPagingJob")
             .listener(inactiveJobListener)
             .start(orderPagingStep)
             .build()
 }
 ```

* JobExecutionListener 인터페이스 구현
* orderPagingJob에서 Bean 주입받아서 리스너 처리
* 인터페이스를 구현하지 않고 @BeforeSte, @AfterStep 어노테이션으로 간단하게 구현 가능