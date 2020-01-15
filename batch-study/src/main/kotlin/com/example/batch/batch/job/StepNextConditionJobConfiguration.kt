package com.example.batch.batch.job

import logger
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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
//                    contribution.exitStatus = ExitStatus.FAILED
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