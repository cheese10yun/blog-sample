package com.example.exposedstudy

import com.example.exposedstudy.Payments.amount
import com.example.exposedstudy.Payments.orderId
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.math.BigDecimal
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.util.StopWatch


class ExposedGettingStarted {
    private val config = HikariConfig().apply {
        jdbcUrl = "jdbc:mysql://localhost:3366/exposed_study?useSSL=false&serverTimezone=UTC&autoReconnect=true&rewriteBatchedStatements=true"
        driverClassName = "com.mysql.cj.jdbc.Driver"
        username = "root"
        password = ""
        maximumPoolSize = 12
    }

    private val dataSource = HikariDataSource(config)

    @Test
    fun `exposed DAO`() {
        // connection to MySQL
        Database.connect(dataSource)

        transaction {
            // Show SQL logging
            addLogger(StdOutSqlLogger)

            // CREATE TABLE IF NOT EXISTS payment (id BIGINT AUTO_INCREMENT PRIMARY KEY, order_id BIGINT NOT NULL, amount DECIMAL(19, 4) NOT NULL)
            SchemaUtils.create(Payments)

            // INSERT INTO payment (amount, order_id) VALUES (1, 1)
            // ...
            (1..20).map {
                Payment.new {
                    amount = it.toBigDecimal()
                    orderId = it.toLong()
                }
            }

            // UPDATE payment SET amount=0 WHERE id = 1
            // ...
            Payment.all()
                    .forEach { it.amount = BigDecimal.ZERO }

            // SELECT payment.id, payment.order_id, payment.amount FROM payment WHERE payment.amount >= 1
            // Payment(amount=1.0000, orderId=1)
            Payment.find { amount eq BigDecimal.ONE }
                    .forEach { println(it) }

            // DELETE FROM payment WHERE payment.id = 1
            // ...
            Payment.all()
                    .forEach { it.delete() }

            // DROP TABLE IF EXISTS payment
            SchemaUtils.drop(Payments)
        }
    }

    @Test
    fun `exposed DSL`() {
        // connection to MySQL
        Database.connect(dataSource)

        transaction {
            // Show SQL logging
            addLogger(StdOutSqlLogger)

            // CREATE TABLE IF NOT EXISTS payment (id BIGINT AUTO_INCREMENT PRIMARY KEY, order_id BIGINT NOT NULL, amount DECIMAL(19, 4) NOT NULL)
            SchemaUtils.create(Payments)

            // INSERT INTO payment (amount, order_id) VALUES (1, 1)
            // ...
            (1..5).map {
                Payments.insert { payments ->
                    payments[amount] = it.toBigDecimal()
                    payments[orderId] = it.toLong()
                }
            }

            // UPDATE payment SET amount=0 WHERE payment.amount >= 0
            Payments.update({ amount greaterEq BigDecimal.ZERO })
            {
                it[amount] = BigDecimal.ZERO
            }

            // SELECT payment.id, payment.order_id, payment.amount FROM payment WHERE payment.amount = 0
            // Payment(amount=1.0000, orderId=1)
            Payments.select { amount eq BigDecimal.ZERO }
                    .forEach { println(it) }

            // DELETE FROM payment WHERE payment.amount >= 1
            Payments.deleteWhere { amount greaterEq BigDecimal.ONE }

            // DROP TABLE IF EXISTS payment
            SchemaUtils.drop(Payments)
        }
    }


    @Test
    fun `create`() {
        Database.connect(dataSource)
        transaction {
            val paymentId = Payments.insertAndGetId {
                it[amount] = 100.toBigDecimal()
                it[orderId] = 1L
            }
            println(paymentId)
        }
    }

    @Test
    fun `read`() {
        Database.connect(dataSource)
        transaction {
            val payment = Payments.select { Payments.id eq 1L }.forEach { println(it) }
        }
    }

    @Test
    fun `read slice`() {
        Database.connect(dataSource)
        transaction {
            val map = Payments
                    .slice(amount, Payments.id)
                    .selectAll()
                    .map {
                        it[amount] to it[Payments.id]
                    }

            println(map)
        }
    }

    @Test
    fun `with distinct`() {
        Database.connect(dataSource)
        transaction {
            val result = Payments
                    .slice(amount)
                    .select { Payments.id less 5 }
                    .withDistinct().map {
                        it[amount]
                    }
            println(result)
        }
    }

    @Test
    fun update() {
        Database.connect(dataSource)
        transaction {
            val update = Payments.update({ Payments.id eq 2L }) { it[amount] = 10000.toBigDecimal() }
            println(update)
        }
    }

    @Test
    fun `batch insert`() {
        Database.connect(dataSource)
        val amounts = (1..100).map { it.toBigDecimal() }
        val stopWatch = StopWatch()

        transaction {
            stopWatch.start()
            Payments.batchInsert(amounts) { amount ->
                this[orderId] = amount.toLong()
                this[Payments.amount] = amount
            }
        }

        stopWatch.stop()
        println(stopWatch.totalTimeSeconds)
    }

    @Test
    @Disabled
    fun `inert`() {
        Database.connect(dataSource)
        val amounts = (1..10_000).map { it.toBigDecimal() }
        val stopWatch = StopWatch()

        transaction {
            stopWatch.start()

            for (amount in amounts) {
                Payments.insert {
                    it[Payments.amount] = amount
                }
            }
        }

        stopWatch.stop()
        println(stopWatch.totalTimeSeconds)
    }

    @Test
    fun `delete`() {
        Database.connect(dataSource)
        transaction {
            val rows = Payments.deleteWhere {
                amount eq 20.toBigDecimal()
            }

            println(rows)
        }
    }

    @Test
    fun `Conditional where`() {

        val targetAmount = 10.toBigDecimal()

        val query = when {
            targetAmount != null ->
                Payments.select { amount eq 10.toBigDecimal() }
            targetAmount > 100.toBigDecimal() -> {
                Payments.select { amount eq 20.toBigDecimal() }
            }
            else -> {
                Payments.select { amount eq 30.toBigDecimal() }
            }
        }
    }

    @Test
    fun `join query`() {

        Database.connect(dataSource)
        transaction {
//            SchemaUtils.create(OrderTable)
//            SchemaUtils.create(PaymentTable)

            val selectAll =
                    Payments.join(Orders, JoinType.INNER, additionalConstraint = { Orders.id eq orderId })
                            .selectAll()
                            .forEach {
                                val bigDecimal = it[amount]
                                println(bigDecimal)
                            }


            Payment.all()
                    .toList()
                    .forEach { println(it.amount) }
        }

    }
}


//object Orders : LongIdTable(name = "orders") {
//    val amount = decimal("amount", 19, 4)
//    val name = varchar("name", 100)
//}
//
//object Payments : LongIdTable(name = "payment") {
//    val orderId = long("order_id")
//    val amount = decimal("amount", 19, 4)
//}
//
//class Payment(id: EntityID<Long>) : LongEntity(id) {
//    companion object : LongEntityClass<Payment>(Payments)
//
//    var amount by Payments.amount
//    var orderId by Payments.orderId
//
//    override fun toString(): String {
//        return "Payment(amount=$amount, orderId=$orderId)"
//    }
//}