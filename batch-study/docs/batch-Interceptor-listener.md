# Batch Interceptor
> 해당 글은 [처음으로 배우는 스프링 부트 2](http://www.hanbit.co.kr/store/books/look.php?p_code=B4458049183), [기억보단 기록을 : Spring Batch Paging Reader 사용시 같은 조건의 데이터를 읽고 수정할때 문제](https://jojoldu.tistory.com/337)을 보고 학습한 내용을 정리한 글입니다.

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