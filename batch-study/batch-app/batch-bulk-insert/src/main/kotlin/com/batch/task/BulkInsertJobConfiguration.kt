package com.batch.task

import com.batch.payment.domain.payment.Payment
import com.batch.payment.domain.payment.PaymentBack
import com.batch.payment.domain.payment.PaymentBackJpa
import com.batch.payment.domain.payment.PaymentBackJpaRepository
import com.batch.task.support.listener.JobReportListener
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaCursorItemReader
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

const val GLOBAL_CHUNK_SIZE = 1000
const val DATA_SET_UP_SIZE = 100

@Configuration
class BulkInsertJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val jobDataSetUpListener: JobDataSetUpListener,
    private val dataSource: DataSource,
    private val exposedDataBase: Database,
    private val paymentBackJpaRepository: PaymentBackJpaRepository,
    private val txService: TxService,
    entityManagerFactory: EntityManagerFactory
) {

    @Bean
    fun bulkInsertJob(
        jdbcTemplateInsertStep: Step
    ): Job =
        jobBuilderFactory["bulkInsertJob"]
            .incrementer(RunIdIncrementer())
            .listener(JobReportListener())
            .listener(jobDataSetUpListener)
            .start(jdbcTemplateInsertStep)
            .build()

    @Bean
    @JobScope
    fun bulkInsertStep(
        stepBuilderFactory: StepBuilderFactory,
        bulkInsertReader: JpaCursorItemReader<Payment>
    ): Step =
        stepBuilderFactory["bulkInsertStep"]
            .chunk<Payment, Payment>(GLOBAL_CHUNK_SIZE)
            .reader(bulkInsertReader)
//            .writer(writerWithStatement)
//            .writer(writerWithExposed)
            .writer(writerWithJpa)
            .build()

    @Bean
    @StepScope
    fun bulkInsertReader(
        entityManagerFactory: EntityManagerFactory
    ) = JpaCursorItemReaderBuilder<Payment>()
        .name("bulkInsertReader")
        .entityManagerFactory(entityManagerFactory)
        .queryString("SELECT p FROM Payment p")
        .build()


    private val writerWithStatement: ItemWriter<Payment> = ItemWriter { payments ->
        val sql = "insert into payment_back (amount, order_id) values (?, ?)"
        val connection = dataSource.connection
        val statement = connection.prepareStatement(sql)!!
        try {
            for (payment in payments) {
                statement.apply {
                    this.setBigDecimal(1, payment.amount)
                    this.setLong(2, payment.orderId)
                    this.addBatch()
                }
            }
            statement.executeBatch()
        } catch (e: Exception) {
            throw e
        } finally {
            if (statement.isClosed.not()) {
                statement.close()
            }
            if (connection.isClosed.not()) {
                connection.close()
            }
        }
    }

    val writerWithJpa: ItemWriter<Payment> =
        ItemWriter {
            txService.save(it)
        }

    private val writerWithExposed: ItemWriter<Payment> = ItemWriter { payments ->
        transaction(
            exposedDataBase
        ) {
            PaymentBack.batchInsert(
                data = payments,
                shouldReturnGeneratedValues = false
            ) { payment ->
                this[PaymentBack.orderId] = payment.orderId
                this[PaymentBack.amount] = payment.amount
            }
        }
    }
}

@Service
class TxService(
    private val paymentBackJpaRepository: PaymentBackJpaRepository
) {

    @Transactional
    fun save(payments: List<Payment>) {
        payments.map { payment ->
            PaymentBackJpa(
                amount = payment.amount,
                orderId = payment.orderId
            )
        }
            .also {
                paymentBackJpaRepository.saveAll(it)
            }
    }
}