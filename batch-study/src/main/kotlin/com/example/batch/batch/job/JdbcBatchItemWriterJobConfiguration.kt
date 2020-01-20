package com.example.batch.batch.job

import com.example.batch.domain.order.domain.Order
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.JdbcCursorItemReader

import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.BeanPropertyRowMapper
import javax.sql.DataSource

@Configuration
class JdbcBatchItemWriterJobConfiguration(
        private val jobBuilderFactory: JobBuilderFactory,
        private val stepBuilderFactory: StepBuilderFactory,
        private val dataSource: DataSource
) {

    private val chunkSize = 10

    @Bean
    fun jdbcBatchWriterJob(): Job {
        return jobBuilderFactory.get("jdbcBatchWriterJob")
                .start(jdbcBatchItemWriterStep())
                .build();
    }

    @Bean
    fun jdbcBatchItemWriterStep(): Step {
        return stepBuilderFactory.get("jdbcBatchItemWriterStep")
                .chunk<Order, Order>(chunkSize)
                .reader(jdbcBatchItemWriterReader())
                .writer(jdbcBatchItemWriter())
                .build()
    }


    @Bean
    fun jdbcBatchItemWriterReader(): JdbcCursorItemReader<Order> {
        return JdbcCursorItemReaderBuilder<Order>()
                .fetchSize(chunkSize)
                .dataSource(dataSource)
                .rowMapper(BeanPropertyRowMapper(Order::class.java))
                .sql("select `id` ,`amount`, `created_at`, `updated_at` from orders")
                .name("jdbcBatchItemWriterReader")
                .build()

    }


    @Bean
    fun jdbcBatchItemWriter(): JdbcBatchItemWriter<Order> {
        return JdbcBatchItemWriterBuilder<Order>()
                .dataSource(dataSource)
                .sql("insert into orders (amount, created_at, updated_at) values (:amount, :created_at, :updated_at)")
                .beanMapped()
                .build();

    }

}