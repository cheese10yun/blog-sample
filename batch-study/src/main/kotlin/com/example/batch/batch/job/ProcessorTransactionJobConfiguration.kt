package com.example.batch.batch.job

import com.example.batch.domain.order.domain.Order
import logger
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
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
class ProcessorTransactionJobConfiguration(
        private val jobBuilderFactory: JobBuilderFactory,
        private val stepBuilderFactory: StepBuilderFactory,
        private val entityManagerFactory: EntityManagerFactory
) {

    private val chunkSize = 20
    private val log by logger()

    @Bean
    fun processorTransactionJob(): Job {
        return jobBuilderFactory.get("processorTransactionJob")
                .incrementer(RunIdIncrementer())
                .start(step())
                .build()
    }

    @Bean
    fun step(): Step {
        return stepBuilderFactory.get("step")
                .chunk<Order, Order>(chunkSize)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build()
    }

    fun reader(): JpaPagingItemReader<Order> {
        return JpaPagingItemReaderBuilder<Order>()
                .name("reader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select o from Order o")
                .pageSize(chunkSize)
                .build()
    }

    fun processor(): ItemProcessor<Order, Order> {
        return ItemProcessor {
            log.info("Item Processor order item size  ----------> ${it.items.size}")
            it
        }
    }

    fun writer(): ItemWriter<Order> {
        return ItemWriter {
            for (order in it) {
                log.info("Item Writer Processor order item size ----------> ${order.items.size}")
            }
        }
    }
}