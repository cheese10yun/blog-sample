package com.cheese.yun.batch.sample

import com.cheese.yun.listener.SlackNotificationListener
import com.cheese.yun.support.logger.logger
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SampleJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    private val log by logger()

    @Bean
    fun sampleJob(): Job {
        return jobBuilderFactory["sampleJob"]
            .incrementer(RunIdIncrementer())
            .listener(SlackNotificationListener())
            .start(sampleStep())
            .build()
    }

    private fun sampleStep(): Step {
        return stepBuilderFactory["sampleStep"]
            .tasklet { contribution, chunkContext ->
                log.info("This is Step 1")
                RepeatStatus.FINISHED
            }
            .build()

    }

}