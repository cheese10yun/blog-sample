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
import java.math.BigDecimal
import javax.persistence.EntityManagerFactory

@Configuration
class ProcessorFilterJobConfiguration(
        private val jobBuilderFactory: JobBuilderFactory,
        private val stepBuilderFactory: StepBuilderFactory,
        private val entityManagerFactory: EntityManagerFactory
) {
    private val chunkSize = 10;
    private val log by logger()

    @Bean
    fun processorFilterJob(): Job {
        return jobBuilderFactory.get("processorFilterJob")
                .incrementer(RunIdIncrementer())
                .start(processorFilterStep())
                .build()
    }

    @Bean
    @JobScope
    fun processorFilterStep(): Step {
        return stepBuilderFactory.get("step")
                .chunk<Order, Order>(chunkSize)
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
    fun processor(): ItemProcessor<Order, Order> {
        return ItemProcessor label@{
            val amount = it.amount
            if (BigDecimal.ZERO == amount.divide(BigDecimal(2))) {
                return@label null
            }
            it
        }
    }

    private fun writer(): ItemWriter<Order> {
        return ItemWriter {
            for (order in it)
                log.info("amount value :  ${order.amount}")
        }
    }
}