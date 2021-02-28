package com.batch.task

import com.batch.payment.domain.payment.Payment
import com.batch.task.support.listener.JobReportListener
import com.batch.task.support.logger
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.database.JpaCursorItemReader
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.math.BigDecimal
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

const val CHUNK_SIZE = 10_000
const val DATA_SET_UP_SIZE = 100_000

@Configuration
class ReaderPerformanceJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    val log by logger()

    @Bean
    fun readerPerformanceJob(
        jobDataSetUpListener: JobDataSetUpListener,
        readerPerformanceStep: Step
    ) =
        jobBuilderFactory["readerPerformanceJob"]
            .incrementer(RunIdIncrementer())
            .listener(JobReportListener())
            .listener(jobDataSetUpListener)
            .start(readerPerformanceStep)
            .build()

    @Bean
    @JobScope
    fun readerPerformanceStep(
        jpaCursorItemReader: JpaCursorItemReader<Payment>
    ) =
        stepBuilderFactory["readerPerformanceStep"]
            .chunk<Payment, Payment>(CHUNK_SIZE)
            .reader(jpaCursorItemReader)
            .writer { log.info("item size ${it.size}") }
            .build()

    @Bean
    @StepScope
    fun jpaCursorItemReader(
        entityManagerFactory: EntityManagerFactory
    ) = JpaCursorItemReaderBuilder<Payment>()
        .name("bulkInsertReader")
        .entityManagerFactory(entityManagerFactory)
        .queryString("SELECT p FROM Payment p")
        .build()
}

@Component
class JobDataSetUpListener(
    private val dataSource: DataSource,
) : JobExecutionListener {
    val log by logger()

    override fun beforeJob(jobExecution: JobExecution) {
        val payments = (1..DATA_SET_UP_SIZE)
            .map { Payment(it.toBigDecimal(), it.toLong()) }

        val sql = "insert into payment (amount, order_id) values (?, ?)"
        val connection = dataSource.connection
        val statement = connection.prepareStatement(sql)!!

        try {
            for (payment in payments) {
                statement.apply {
                    setBigDecimal(1, BigDecimal.ZERO)
                    setLong(2, payment.orderId)
                    addBatch()
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
        log.info("data set up done")
    }

    override fun afterJob(jobExecution: JobExecution): Unit = Unit
}