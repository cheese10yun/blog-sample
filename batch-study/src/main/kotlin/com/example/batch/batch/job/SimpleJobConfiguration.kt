package com.example.batch.batch.job

import logger
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
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
//                    throw IllegalArgumentException("asd")
                    RepeatStatus.FINISHED
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