package com.example.batch.batch.job

import com.example.batch.batch.core.InvertedOrderingItemReader
import com.example.batch.domain.order.dao.OrderRepository
import com.example.batch.domain.order.domain.Order
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort
import java.math.BigDecimal
import javax.persistence.EntityManagerFactory

@Configuration
class InvertedOrderingItemReaderJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val entityManagerFactory: EntityManagerFactory,
    private val orderRepository: OrderRepository
) {

    val chunkSize = 10

    @Bean
    fun invertedOrderingItemReaderJob(): Job {
        return jobBuilderFactory.get("invertedOrderingItemReaderJob")
            .incrementer(RunIdIncrementer())
            .start(step())
            .build()
    }

    private fun step(): Step {
        return stepBuilderFactory.get("step")
            .chunk<Order, Order>(chunkSize)
            .reader(reader())
            .writer(writer())
            .build()
    }

    private fun reader(): InvertedOrderingItemReader<Order> {
        return InvertedOrderingItemReader(
            "InvertedOrderingItemReader",
            Sort.by(Sort.Direction.ASC, "id"),
            chunkSize
        ) { pageable ->
            orderRepository.findByAmountGreaterThan(BigDecimal.TEN, pageable)
        }
    }

    private fun writer(): ItemWriter<Order> {
        return ItemWriter { orders ->
            println("=================")
            for (order in orders) {
                println("orders -> ${order.id!!}")
            }
            println("=================")
        }
    }
}