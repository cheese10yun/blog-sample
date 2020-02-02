package com.example.batch.batch.job

import com.example.batch.batch.core.PageApiItemReader
import com.example.batch.common.PageResponse
import com.example.batch.domain.order.domain.Payment
import com.example.batch.service.PaymentRestService
import logger
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal

@Configuration
class PagingApiReaderJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val paymentRestService: PaymentRestService
) {

    private val log by logger()
    private val chunkSize = 100


    @Bean
    fun pagingApiReaderJob(): Job {
        return jobBuilderFactory.get("pagingApiReaderJob")
            .incrementer(RunIdIncrementer())
            .start(step())
            .build()

    }

    private fun step(): Step {
        return stepBuilderFactory.get("pagingApiReaderStep")
            .chunk<PageResponse<Payment>, Payment>(chunkSize)
            .reader(reader())
            .writer(writer())
            .build()
    }

    private fun reader(): ItemReader<out PageResponse<Payment>> {
        return PageApiItemReader(
            size = chunkSize,
            page = 0,
            amount = BigDecimal(100),
            paymentRestService = paymentRestService
        )
    }

    private fun writer(): ItemWriter<Payment> {
        return ItemWriter {
            println("asdasd")
        }
    }
}