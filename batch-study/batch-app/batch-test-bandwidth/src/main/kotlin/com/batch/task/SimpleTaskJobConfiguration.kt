package com.batch.task

import com.batch.payment.domain.payment.Payment
import com.batch.task.support.listener.JobReportListener
import com.batch.task.support.logger
import java.time.LocalDateTime
import javax.persistence.EntityManagerFactory
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaCursorItemReader
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.context.annotation.Bean

const val CHUNK_SIZE = 10
private val localDateTime = LocalDateTime.of(2021, 6, 1, 0, 0, 0)

class SimpleTaskJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    val log by logger()


    @Bean
    fun simpleTaskJob(
        readerPerformanceStep: Step
    ) =
        jobBuilderFactory["readerPerformanceJob"]
            .incrementer(RunIdIncrementer())
            .start(readerPerformanceStep)
            .build()


    @Bean
    @JobScope
    fun simpleTaskStep(
        stepBuilderFactory: StepBuilderFactory,
        jpaPagingItemReader: JpaPagingItemReader<Payment>,
        writerWithJpa: ItemWriter<Payment>
    ): Step =
        stepBuilderFactory["simpleTaskStep"]
            .chunk<Payment, Payment>(CHUNK_SIZE)
            .reader(jpaPagingItemReader)
            .writer(writerWithJpa)
            .build()


    @Bean
    @StepScope
    fun jpaPagingItemReader(
        entityManagerFactory: EntityManagerFactory
    ) = JpaPagingItemReaderBuilder<Payment>()
        .name("jpaPagingItemReader")
        .pageSize(CHUNK_SIZE)
        .entityManagerFactory(entityManagerFactory)
        .queryString("SELECT p FROM Payment p where p.createdAt >= :createdAt ORDER BY p.createdAt DESC")
        .parameterValues(mapOf("createdAt" to localDateTime))
        .build()

    @Bean
    @StepScope
    fun writerWithJpa(
    ): ItemWriter<Payment> = ItemWriter { payments ->
        println("sie: ${payments.size}")
    }
}