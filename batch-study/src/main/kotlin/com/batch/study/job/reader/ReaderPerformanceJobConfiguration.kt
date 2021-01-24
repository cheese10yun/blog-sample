package com.batch.study.job.reader

import com.batch.study.domain.payment.Payment
import com.batch.study.listener.GLOBAL_CHUNK_SIZE
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.validation.annotation.Validated
import javax.persistence.EntityManagerFactory
import javax.validation.constraints.NotEmpty

@Configuration
class ReaderPerformanceJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    entityManagerFactory: EntityManagerFactory
) {

    @Bean
    @Primary
    fun readerPerformanceJob(
        readerPerformanceStep: Step
    ): Job =
        jobBuilderFactory["readerPerformanceJob"]
            .incrementer(RunIdIncrementer())
            .start(readerPerformanceStep)
            .build()

    @Bean
    @JobScope
    fun readerPerformanceStep(
        properties: ReaderPerformanceJobProperties
    ): Step =
        stepBuilderFactory["readerPerformanceStep"]
            .chunk<Payment, Payment>(GLOBAL_CHUNK_SIZE)
            .reader(jpaPagingItemReader)
            .writer {
                it.forEach { payment ->
                    println(payment)
                }
            }
            .build()

    val jpaPagingItemReader: JpaPagingItemReader<Payment> =
        JpaPagingItemReaderBuilder<Payment>()
            .queryString("SELECT p FROM Payment p")
            .entityManagerFactory(entityManagerFactory)
            .name("jpaPagingItemReader")
            .build()

    private val writer: ItemWriter<Payment> = ItemWriter {
        it.forEach { payment ->
            println("===============")
            println(payment)
            println("===============")
        }
    }
}

@ConstructorBinding
@ConfigurationProperties(prefix = "args")
@Validated
class ReaderPerformanceJobProperties(
    @field:NotEmpty
    val value: String
)
