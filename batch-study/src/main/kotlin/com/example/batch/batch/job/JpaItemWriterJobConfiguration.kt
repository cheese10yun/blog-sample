package com.example.batch.batch.job

import com.example.batch.domain.order.domain.Order
import com.example.batch.domain.order.domain.Payment
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
class JpaItemWriterJobConfiguration(
        private val jobBuilderFactory: JobBuilderFactory,
        private val stepBuilderFactory: StepBuilderFactory,
        private val entityManagerFactory: EntityManagerFactory
) {

    private val chunkSize = 10

    @Bean
    fun jpaItemWriterJob(): Job {
        return jobBuilderFactory.get("jpaItemWriterJob")
                .incrementer(RunIdIncrementer())
                .start(jpaItemWriterStep())
                .build()
    }

    @Bean
    fun jpaItemWriterStep(): Step {
        return stepBuilderFactory.get("jpaItemWriterStep")
                .chunk<Order, Payment>(10)
                .reader(jpaItemWriterReader())
                .processor(jpaItemProcessor())
//                .writer(jpaItemWriter())
                .writer(customItemWriter())
                .build()
    }

    @Bean
    fun jpaItemWriterReader(): JpaPagingItemReader<Order> {
        return JpaPagingItemReaderBuilder<Order>()
                .name("jpaItemWriterReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT o From Order o")
                .build()
    }

    @Bean
    fun jpaItemProcessor(): ItemProcessor<Order, Payment> {
        return ItemProcessor { order: Order -> Payment(order.amount) }
    }

    //    @Bean
//    fun jpaItemWriter(): JpaItemWriter<Order2> {
//        val itemWriter = JpaItemWriter<Order2>()
//        itemWriter.setEntityManagerFactory(entityManagerFactory)
//        return itemWriter;
//    }
    @Bean
    fun customItemWriter(): ItemWriter<Payment> {
        return ItemWriter { items ->
            for (item in items) {
                println(item.amount)
            }
        }

    }
}