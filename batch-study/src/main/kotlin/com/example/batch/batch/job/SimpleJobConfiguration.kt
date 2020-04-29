package com.example.batch.batch.job

import com.example.batch.batch.listener.JobExecutionNotificationListener
import logger
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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
            .listener(JobExecutionNotificationListener())
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