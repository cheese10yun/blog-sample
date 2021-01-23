package com.batch.study.job.reader

import com.batch.study.domain.payment.Payment
import com.batch.study.listener.GLOBAL_CHUNK_SIZE
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.persistence.EntityManagerFactory

@Configuration
class ReaderPerformanceJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    entityManagerFactory: EntityManagerFactory
) {

    @Bean
    @JobScope
    fun readerPerformanceJob(
        readerPerformanceStep: Step
    ): Job =
        jobBuilderFactory["readerPerformanceJob"]
            .start(readerPerformanceStep)
            .build()

    @Bean
    @StepScope
    fun readerPerformanceStep(): Step =
        stepBuilderFactory["readerPerformanceStep"]
            .chunk<Payment, Payment>(GLOBAL_CHUNK_SIZE)
            .reader(jpaPagingItemReader)
            .writer(writer)
            .build()

    val jpaPagingItemReader: JpaPagingItemReader<Payment> =
        JpaPagingItemReaderBuilder<Payment>()
            .queryString("SELECT p FROM Payment p")
            .entityManagerFactory(entityManagerFactory)
            .name("jpaPagingItemReader")
            .build()

    private val writer: ItemWriter<Payment> = ItemWriter {}
}