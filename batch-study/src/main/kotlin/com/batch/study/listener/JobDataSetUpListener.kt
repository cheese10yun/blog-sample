package com.batch.study.listener

import com.batch.study.domain.payment.Payment
import com.batch.study.logger
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.stereotype.Component
import java.sql.Connection
import javax.sql.DataSource

@Component
class JobDataSetUpListener(
    private val dataSource: DataSource,
) : JobExecutionListener {

    val log by logger()

    override fun beforeJob(jobExecution: JobExecution) {
        val payments = (1..500_000)
            .map { Payment(it.toBigDecimal(), it.toLong()) }

        insert(payments)
        log.info("data set up done")
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
        val sql = "insert into payment (amount, order_id) values (?, ?)"
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

    override fun afterJob(jobExecution: JobExecution): Unit = Unit
}

