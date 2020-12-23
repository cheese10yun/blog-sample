package com.batch.study.job

import com.batch.study.GLOBAL_CHUNK_SIZE
import com.batch.study.domain.payment.Payment
import com.batch.study.domain.payment.PaymentBack
import com.batch.study.listener.JobDataSetUpListener
import com.batch.study.listener.JobReportListener
import io.reactivex.rxkotlin.toFlowable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.HibernateCursorItemReader
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.sql.Connection.TRANSACTION_REPEATABLE_READ
import javax.persistence.EntityManagerFactory

@Configuration
class BatchInsertExposedJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val jobDataSetUpListener: JobDataSetUpListener,
    private val exposedDataBase: Database,
    entityManagerFactory: EntityManagerFactory
) {

    @Bean
    fun batchInsertJob(
        batchInsertExposedStep: Step
    ): Job =
        jobBuilderFactory["batchInsertJob"]
            .incrementer(RunIdIncrementer())
            .listener(JobReportListener())
            .listener(jobDataSetUpListener)
            .start(batchInsertExposedStep)
            .build()

    @Bean
    @JobScope
    fun batchInsertExposedStep(
        stepBuilderFactory: StepBuilderFactory,
        cursorItemReader: HibernateCursorItemReader<Payment>
    ): Step =
        stepBuilderFactory["batchInsertExposedStep"]
            .chunk<Payment, Payment>(GLOBAL_CHUNK_SIZE)
            .reader(cursorItemReader)
            .writer(writer2)
            .build()

    private val reader: JpaPagingItemReader<Payment> =
        JpaPagingItemReaderBuilder<Payment>()
            .queryString("SELECT p FROM Payment p")
            .entityManagerFactory(entityManagerFactory)
            .name("readerPayment")
            .build()

    private val writer: ItemWriter<Payment> = ItemWriter { payments ->
        payments
            .toFlowable()
            .parallel()
            .runOn(Schedulers.io())
            .map {
                println("mapping : ${Thread.currentThread().name}")
                it
            }
            .sequential()
            .toList()
            .observeOn(Schedulers.computation())
            .subscribe(
                {
                    println("Received : ${Thread.currentThread().name}")
                    insert(it)
                },
                {}
            )
    }

    private val writer2: ItemWriter<Payment> = ItemWriter { payments ->
        insert(payments)
    }

    private fun insert(payments: List<Payment>) {
        transaction(
            exposedDataBase
        ) {
            PaymentBack.batchInsert(
                data = payments,
                shouldReturnGeneratedValues = true
            ) { payment ->
                this[PaymentBack.orderId] = payment.orderId
                this[PaymentBack.amount] = payment.amount
            }
        }
    }
}