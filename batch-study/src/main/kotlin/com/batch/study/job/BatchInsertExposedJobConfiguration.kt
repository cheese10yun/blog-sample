package com.batch.study.job

import com.batch.study.domain.payment.Payment
import com.batch.study.domain.payment.PaymentBack
import com.batch.study.listener.JobDataSetUpListener
import com.batch.study.listener.JobReportListener
import com.batch.study.logger
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
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

@Configuration
class BatchInsertExposedJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val jobDataSetUpListener: JobDataSetUpListener,
    private val dataSource: DataSource,
    entityManagerFactory: EntityManagerFactory
) {
    private val CHUNK_SZIE = 1_000
    private val log by logger()

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
        stepBuilderFactory: StepBuilderFactory
    ): Step =
        stepBuilderFactory["batchInsertExposedStep"]
            .chunk<Payment, Payment>(CHUNK_SZIE)
            .reader(reader)
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
        Database.connect(dataSource)
        transaction {
            PaymentBack.batchInsert(payments) { payment ->
                this[PaymentBack.orderId] = payment.orderId
                this[PaymentBack.amount] = payment.amount
            }
        }
    }
}