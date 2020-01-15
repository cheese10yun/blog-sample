package com.example.batch.batch.job

import com.example.batch.batch.listener.InactiveJobListener
import logger
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.flow.FlowExecutionStatus
import org.springframework.batch.core.job.flow.JobExecutionDecider
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.lang.Nullable
import java.util.*

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