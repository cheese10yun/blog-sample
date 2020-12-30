package com.batch.study

import com.batch.study.domain.payment.Payment
import com.batch.study.domain.payment.PaymentBack
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.util.StopWatch
import java.sql.Connection
import javax.sql.DataSource

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles("test")
class PaymentBackTest(
    private val exposedDataBase: Database,
    private val dataSource: DataSource,
) {

    val log by logger()

    val intRange = 1..10_000

    @Test
    fun `exposed batch`() {
        val payments = intRange
            .map { Payment(it.toBigDecimal(), it.toLong()) }

        val stopWatch = StopWatch()
        stopWatch.start()
        transaction(db = exposedDataBase,) {

            PaymentBack.batchInsert(
                data = payments,
                shouldReturnGeneratedValues = false,
            ) { payment ->
                this[PaymentBack.orderId] = payment.orderId
                this[PaymentBack.amount] = payment.amount
            }
        }
        stopWatch.stop()

        print(stopWatch)
    }

    @Test
    internal fun `batch statement`() {
        val stopWatch = StopWatch()
        stopWatch.start()

        val payments = (intRange)
            .map { Payment(it.toBigDecimal(), it.toLong()) }

        insert(payments)

        stopWatch.stop()
        print(stopWatch)
    }

    private fun print(stopWatch: StopWatch) {
        println("Seconds: ${stopWatch.totalTimeSeconds}")
        println("Millis: ${stopWatch.totalTimeMillis}")
    }

    private fun insert(payments: List<Payment>) {
        val connection = dataSource.connection
        val batchStatement = BatchStatement(connection)

        connection.autoCommit = false
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
}