package com.batch.study.job

import com.batch.study.GLOBAL_CHUNK_SIZE
import com.batch.study.domain.payment.Payment
import com.batch.study.listener.JobDataSetUpListener
import com.batch.study.listener.JobReportListener
import org.hibernate.SessionFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.HibernateCursorItemReader
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.HibernateCursorItemReaderBuilder
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.sql.Connection
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

@Configuration
class JdbcTemplateInsertJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val jobDataSetUpListener: JobDataSetUpListener,
    private val dataSource: DataSource,
    entityManagerFactory: EntityManagerFactory
) {

    @Bean
    fun jdbcTemplateInsertJob(
        jdbcTemplateInsertStep: Step
    ): Job =
        jobBuilderFactory["jdbcTemplateInsertJob"]
            .incrementer(RunIdIncrementer())
            .listener(JobReportListener())
            .listener(jobDataSetUpListener)
            .start(jdbcTemplateInsertStep)
            .build()

    @Bean
    @JobScope
    fun jdbcTemplateInsertStep(
        stepBuilderFactory: StepBuilderFactory,
        cursorItemReader: HibernateCursorItemReader<Payment>
    ): Step =
        stepBuilderFactory["jdbcTemplateInsertStep"]
            .chunk<Payment, Payment>(GLOBAL_CHUNK_SIZE)
            .reader(cursorItemReader)
            .writer(writer)
            .build()

    @Bean
    @StepScope
    fun cursorItemReader(
        sessionFactory: SessionFactory,
    ): HibernateCursorItemReader<Payment> =
        HibernateCursorItemReaderBuilder<Payment>()
            .name("hibernateCursorItemReader")
            .sessionFactory(sessionFactory)
            .queryString("FROM Payment")
            .build()

    private val reader: JpaPagingItemReader<Payment> =
        JpaPagingItemReaderBuilder<Payment>()
            .queryString("SELECT p FROM Payment p")
            .entityManagerFactory(entityManagerFactory)
            .name("readerPayment")
            .build()

    private val writer: ItemWriter<Payment> = ItemWriter { payments ->
        insert(payments)
    }

    private fun insert(payments: List<Payment>) {
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
}