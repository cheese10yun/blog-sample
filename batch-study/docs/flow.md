# Flow
> 해당 글은 [처음으로 배우는 스프링 부트 2](http://www.hanbit.co.kr/store/books/look.php?p_code=B4458049183)을 보고 학습한 내용을 정리한 글입니다.

Step의 가장 기본적인 흐름은 `읽기-처리-쓰기` 입니다. 여기서 세부적인 조건에 따라서 Step의 실행 여부를 정할 수 있습니다. 이런 흐름은 제어하는 것이 Flow입니다.

![](https://github.com/cheese10yun/TIL/raw/master/assets/batch-flow.png)

위 흐름에서 조건에 해당하는 부분을 JobExecutionDecider 인터페이스를 사용해 구현할 수 있습니다. JobExecutionDecider 인터페이스는 `decide()` 메서드 하나만 제공합니다.

```java
public interface JobExecutionDecider {
	FlowExecutionStatus decide(JobExecution jobExecution, @Nullable StepExecution stepExecution);
}
```
`decide()` 메서드의 반환값으로 FlowExecutionStatus 객체를 반환하도록 명시되어 있습니다. FlowExecutionStatus 객체는 Statu의 값 `COMPLETED`, `STOPPED`, `FAILED`. `UNKOWN` 등을 제공합니다.


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

    private val CHUNK_SZIE: Int = 5

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
