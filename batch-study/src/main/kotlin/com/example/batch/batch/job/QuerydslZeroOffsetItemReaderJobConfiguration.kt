package com.example.batch.batch.job

import com.example.batch.batch.core.Asc
import com.example.batch.batch.core.Desc
import com.example.batch.batch.core.QuerydslZeroOffsetItemReader
import com.example.batch.domain.order.domain.Order
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.persistence.EntityManagerFactory
import com.example.batch.domain.order.domain.QOrder.order as qOrder

@Configuration
class QuerydslZeroOffsetItemReaderJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val entityManagerFactory: EntityManagerFactory
) {

    private val chunkSize = 5000

    @Bean
    fun querydslZeroOffsetItemReaderJob(): Job {
        return jobBuilderFactory.get("querydslZeroOffsetItemReaderJob")
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

    private fun reader(): QuerydslZeroOffsetItemReader<Order> {
        return QuerydslZeroOffsetItemReader(
            name = "QuerydslZeroOffsetItemReader",
            pageSize = chunkSize,
            entityManagerFactory = entityManagerFactory,
            expression = Desc,
            field = qOrder.id
        ) {
            it.selectFrom(qOrder)
        }
    }

    private fun writer(): ItemWriter<Order> {
        return ItemWriter {
            println(it[0].id)
        }
    }
}