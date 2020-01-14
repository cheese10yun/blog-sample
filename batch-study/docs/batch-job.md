> [spring batch in action : 메타테이블엿보기](https://github.com/jojoldu/spring-batch-in-action/blob/master/3_%EB%A9%94%ED%83%80%ED%85%8C%EC%9D%B4%EB%B8%94%EC%97%BF%EB%B3%B4%EA%B8%B0.md)을 보고 정리한 글입니다.

# Job
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

BATCH_JOB_EXECUTION 테이블을 보면 STATUS 칼럼이 FAILED인것을 확인 할 수 있습니다. 이제 다시 `throw IllegalArgumentException("asd")` 주석을 진행하고 정상적으로 Step1이 정상 동작하게 변경합니다.

STEP_EXECUTION_ID | VERSION | STEP_NAME | JOB_EXECUTION_ID | START_TIME | END_TIME | STATUS | COMMIT_COUNT | READ_COUNT | FILTER_COUNT | WRITE_COUNT | READ_SKIP_COUNT | WRITE_SKIP_COUNT | PROCESS_SKIP_COUNT | ROLLBACK_COUNT | EXIT_CODE | EXIT_MESSAGE | LAST_UPDATED
------------------|---------|-----------|------------------|------------|----------|--------|--------------|------------|--------------|-------------|-----------------|------------------|--------------------|----------------|-----------|--------------|-------------
8 | 3 | simpleStep1 | 8 | 2020-01-14 16:41:49 | 2020-01-14 16:41:49 | COMPLETED | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | COMPLETED |  | 2020-01-14 16:41:49
9 | 3 | simpleStep1 | 8 | 2020-01-14 16:41:49 | 2020-01-14 16:41:49 | COMPLETED | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | COMPLETED |  | 2020-01-14 16:41:49

동일한 Job Parameter로 2번 실행했는데 같은 파라미터로 실행되었다는 에러가 발생하지 않습니다. **즉 Spring Batch에서는 동일한 Job Parameter로 성공한 기록이 있을 경우에만 재실행 되지 않습니다.**