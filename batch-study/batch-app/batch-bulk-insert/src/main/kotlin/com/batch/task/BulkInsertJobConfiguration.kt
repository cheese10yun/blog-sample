package com.batch.task

import com.batch.payment.domain.payment.Payment
import com.batch.payment.domain.payment.PaymentBackJpa
import com.batch.task.core.listener.JobReportListener
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.sql.Connection
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

const val GLOBAL_CHUNK_SIZE = 100

@Configuration
class BulkInsertJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val jobDataSetUpListener: JobDataSetUpListener,
    private val dataSource: DataSource,
//    private val exposedDataBase: Database,
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
        bulkInsertReader: JpaPagingItemReader<Payment>
    ): Step =
        stepBuilderFactory["bulkInsertStep"]
            .chunk<Payment, Payment>(GLOBAL_CHUNK_SIZE)
            .reader(bulkInsertReader)
            .writer(writerWithStatement)
            .build()

    @Bean
    @StepScope
    fun bulkInsertReader(
        entityManagerFactory: EntityManagerFactory
    ): JpaPagingItemReader<Payment> =
        JpaPagingItemReaderBuilder<Payment>()
            .queryString("SELECT p FROM Payment p")
            .entityManagerFactory(entityManagerFactory)
            .name("bulkInsertReader")
            .build()


    private val writerWithStatement: ItemWriter<Payment> = ItemWriter { payments ->
        insertWithStatement(payments)
    }

    private val writerWithJpa: JpaItemWriter<PaymentBackJpa> =
        JpaItemWriterBuilder<PaymentBackJpa>()
            .entityManagerFactory(entityManagerFactory)
            .build()

    private fun insertWithStatement(payments: List<Payment>) {
        val connection = dataSource.connection
        val batchStatement = BatchStatement(connection)

        try {
            for (payment in payments) {
                batchStatement.addBatch(payment)
            }
            batchStatement.statement.executeBatch()
        } catch (ex: Exception) {
            throw ex
        } finally {
            batchStatement.close()
            if (connection.isClosed.not()) {
                connection.close()
            }
        }
    }

    private class BatchStatement(connection: Connection) {
        val sql = "insert into payment_back (amount, order_id) values (?, ?)"
        val statement = connection.prepareStatement(sql)!!

        fun addBatch(payment: Payment) = statement.apply {
            this.setBigDecimal(1, payment.amount)
            this.setLong(2, payment.orderId)
            this.addBatch()
        }

        fun close() {
            if (statement.isClosed.not())
                statement.close()
        }
    }

//    private val writerWithExposed: ItemWriter<Payment> = ItemWriter { payments ->
//        transaction(
//            exposedDataBase
//        ) {
//            PaymentBack.batchInsert(
//                data = payments,
//                shouldReturnGeneratedValues = false
//            ) { payment ->
//                this[PaymentBack.orderId] = payment.orderId
//                this[PaymentBack.amount] = payment.amount
//            }
//        }
//    }
}