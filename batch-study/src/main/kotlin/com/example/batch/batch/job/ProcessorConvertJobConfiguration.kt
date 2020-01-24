package com.example.batch.batch.job

import com.example.batch.domain.order.domain.Order
import logger
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.persistence.EntityManagerFactory

@Configuration
class ProcessorConvertJobConfiguration(
        private val jobBuilderFactory: JobBuilderFactory,
        private val stepBuilderFactory: StepBuilderFactory,
        private val entityManagerFactory: EntityManagerFactory
) {

    private val chunkSize = 100
    private val log by logger()

    @Bean
    fun processorConvertJob(): Job {
        return jobBuilderFactory
                .get("processorConvertJob")
                .incrementer(RunIdIncrementer())
                .start(processorConvertStep())
                .build()
    }

    @Bean
    @JobScope
    fun processorConvertStep(): Step {
        return stepBuilderFactory
                .get("processorConvertStep")
                .chunk<Order, String>(chunkSize)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build()
    }

    @Bean
    fun reader(): JpaPagingItemReader<Order> {
        return JpaPagingItemReaderBuilder<Order>()
                .name("reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("select o from Order o")
                .build()
    }

    @Bean
    fun processor(): ItemProcessor<Order, String> {
        return ItemProcessor {
            it.amount.toString()
        }
    }

    fun writer(): ItemWriter<String> {
        return ItemWriter { items ->
            for (item in items) {
                log.info("amount value = {}", item)
            }
        }
    }
}