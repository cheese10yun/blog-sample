package com.example.exposedstudy

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.springframework.util.StopWatch


internal class ExposedTest {

    private val config = HikariConfig().apply {
        jdbcUrl = "jdbc:mysql://localhost:3366/exposed?useSSL=false&serverTimezone=UTC&autoReconnect=true&rewriteBatchedStatements=true"
        driverClassName = "com.mysql.cj.jdbc.Driver"
        username = "root"
        password = ""
        maximumPoolSize = 12

    }

    private val dataSource = HikariDataSource(config)


    @Test
    internal fun `exposed test`() {

        Database.connect(dataSource) //        SchemaUtils.create(Cities)

        transaction { // print sql to std-out
            addLogger(StdOutSqlLogger) //
            SchemaUtils.create(PaymentTable) // insert new city. SQL: INSERT INTO Cities (name) VALUES ('St. Petersburg')
            val city = Payment.new {
                amount = 100.toBigDecimal()
            }

            city.toString()

            (1..20).map {
                Payment.new { amount = it.toBigDecimal() }
            }

            // 'select *' SQL: SELECT Cities.id, Cities.name FROM Cities
            println("Cities: ${Payment.all()}")
        }
    }

    @Test
    internal fun `create`() {
        Database.connect(dataSource)
        transaction {
            val paymentId = PaymentTable.insertAndGetId {
                it[amount] = 100.toBigDecimal()
            }
            println(paymentId)
        }
    }

    @Test
    internal fun `read`() {
        Database.connect(dataSource)
        transaction {
            val payment = PaymentTable.select { PaymentTable.id eq 1 }.forEach { println(it) }
        }
    }

    @Test
    internal fun `read slice`() {
        Database.connect(dataSource)
        transaction {
            val map = PaymentTable
                    .slice(PaymentTable.amount, PaymentTable.id)
                    .selectAll()
                    .map {
                        it[PaymentTable.amount] to it[PaymentTable.id]
                    }

            println(map)
        }
    }

    @Test
    internal fun `with distinct`() {
        Database.connect(dataSource)
        transaction {
            val result = PaymentTable
                    .slice(PaymentTable.amount)
                    .select { PaymentTable.id less 5 }
                    .withDistinct().map {
                        it[PaymentTable.amount]
                    }
            println(result)
        }
    }

    @Test
    internal fun update() {
        Database.connect(dataSource)
        transaction {
            val update = PaymentTable.update({ PaymentTable.id eq 2L }) { it[amount] = 10000.toBigDecimal() }
            println(update)
        }
    }

    @Test
    internal fun `batch insert`() {
        Database.connect(dataSource)
        val amounts = (1..10_000).map { it.toBigDecimal() }
        val stopWatch = StopWatch()

        transaction {
            stopWatch.start()
            PaymentTable.batchInsert(amounts) { amount ->
                this[PaymentTable.amount] = amount
            }
        }

        stopWatch.stop()
        println(stopWatch.totalTimeSeconds)
    }

    @Test
    internal fun `inert`() {
        Database.connect(dataSource)
        val amounts = (1..10_000).map { it.toBigDecimal() }
        val stopWatch = StopWatch()

        transaction {
            stopWatch.start()

            for (amount in amounts) {
                PaymentTable.insert {
                    it[PaymentTable.amount] = amount
                }
            }
        }

        stopWatch.stop()
        println(stopWatch.totalTimeSeconds)
    }
}

object PaymentTable : LongIdTable(name = "payment") {
    val amount = decimal("amount", 19, 4)
}

class Payment(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Payment>(PaymentTable)

    var amount by PaymentTable.amount

    override fun toString(): String {
        return "City(name='$amount')"
    }
}