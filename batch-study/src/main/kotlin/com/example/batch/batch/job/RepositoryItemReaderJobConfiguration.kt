package com.example.batch.batch.job

import com.example.batch.domain.order.dao.OrderRepository
import com.example.batch.domain.order.domain.Order
import logger
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import javax.persistence.EntityManagerFactory
import kotlin.collections.HashMap

@Configuration
class RepositoryItemReaderJobConfiguration(
        private val jobBuilderFactory: JobBuilderFactory,
        private val stepBuilderFactory: StepBuilderFactory,
        private val orderRepository: OrderRepository
) {
    private val chunkSize = 100
    private val log by logger()

    @Bean
    fun repositoryItemReaderJob(): Job {
        return jobBuilderFactory
                .get("repositoryItemReaderJob")
                .incrementer(RunIdIncrementer())
                .start(step())
                .build()
    }

    fun step(): Step {
        return stepBuilderFactory
                .get("step")
                .chunk<Order, Order>(chunkSize)
                .reader(reader())
                .processor(processor())
                .writer(write())
                .build()
    }


    private fun reader(): RepositoryItemReader<Order> {
        return RepositoryItemReaderBuilder<Order>()
                .name("reader")
                .repository(orderRepository)
                .methodName("findByAmountGreaterThan")
                .arguments(listOf(BigDecimal.ZERO))
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .saveState(false)
                .pageSize(chunkSize)
                .maxItemCount(1000)
                .build()
    }

    private fun processor(): ItemProcessor<Order, Order> {
        return ItemProcessor {
            log.info("ItemProcessor ->>>>>>>>>>>>>>>> ${it.amount}")
            it
        }

    }


    private fun write(): ItemWriter<Order> {
        return ItemWriter {
            for (order in it) {
                log.info("ItemWriter ->>>>>>>>>>>>>>> ${order.amount}")
            }
        }
    }
}