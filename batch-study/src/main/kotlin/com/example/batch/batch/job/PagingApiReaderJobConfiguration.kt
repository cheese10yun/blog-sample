package com.example.batch.batch.job

import com.example.batch.batch.core.PageApiItemReader
import com.example.batch.domain.order.dao.PaymentRepository
import com.example.batch.domain.order.domain.Payment
import com.example.batch.service.PaymentRestService
import logger
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal
import java.util.function.Consumer

@Configuration
class PagingApiReaderJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val paymentRestService: PaymentRestService,
    private val paymentRepository: PaymentRepository
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
            .chunk<Payment, Payment>(chunkSize)
            .reader(reader())
            .processor(processor())
            .writer(writer())
            .build()
    }

    private fun reader(): ItemReader<Payment> {
        return PageApiItemReader(
            size = chunkSize,
            page = 0,
            amount = BigDecimal(100),
            restService = paymentRestService

        )
    }

    private fun processor(): ItemProcessor<Payment, Payment> {
        return ItemProcessor {
            println("adasd")
            Payment(it.amount)
        }
    }

    private fun writer(): ItemWriter<Payment> {
        return ItemWriter {

            println("")
            it.forEach(Consumer { t ->
                println(t)
            })
        }
    }
}