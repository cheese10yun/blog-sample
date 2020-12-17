package com.example.exposedstudy

import com.example.exposedstudy.PaymentTable.amount
import com.example.exposedstudy.PaymentTable.orderId
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.util.StopWatch


class ExposedTest {

    private val config = HikariConfig().apply {
        jdbcUrl = "jdbc:mysql://localhost:3366/exposed_study?useSSL=false&serverTimezone=UTC&autoReconnect=true&rewriteBatchedStatements=true"
        driverClassName = "com.mysql.cj.jdbc.Driver"
        username = "root"
        password = ""
        maximumPoolSize = 12

    }

    private val dataSource = HikariDataSource(config)


    @Test
    fun `exposed test`() {

        Database.connect(dataSource) //        SchemaUtils.create(Cities)

        transaction { // print sql to std-out
            addLogger(StdOutSqlLogger) //
            SchemaUtils.create(PaymentTable) // insert new city. SQL: INSERT INTO Cities (name) VALUES ('St. Petersburg')
            val city = Payment.new {
                amount = 100.toBigDecimal()
                orderId = 123L
            }

            city.toString()

            (1..20).map {
                Payment.new {
                    amount = it.toBigDecimal()
                    orderId = it.toLong()
                }
            }

            // 'select *' SQL: SELECT Cities.id, Cities.name FROM Cities
            println("Cities: ${Payment.all()}")
        }
    }

    @Test
    fun `create`() {
        Database.connect(dataSource)
        transaction {
            val paymentId = PaymentTable.insertAndGetId {
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
            val payment = PaymentTable.select { PaymentTable.id eq 1 }.forEach { println(it) }
        }
    }

    @Test
    fun `read slice`() {
        Database.connect(dataSource)
        transaction {
            val map = PaymentTable
                    .slice(amount, PaymentTable.id)
                    .selectAll()
                    .map {
                        it[amount] to it[PaymentTable.id]
                    }

            println(map)
        }
    }

    @Test
    fun `with distinct`() {
        Database.connect(dataSource)
        transaction {
            val result = PaymentTable
                    .slice(amount)
                    .select { PaymentTable.id less 5 }
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
            val update = PaymentTable.update({ PaymentTable.id eq 2L }) { it[amount] = 10000.toBigDecimal() }
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
            PaymentTable.batchInsert(amounts) { amount ->
                this[orderId] = amount.toLong()
                this[PaymentTable.amount] = amount

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
                PaymentTable.insert {
                    it[PaymentTable.amount] = amount
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
            val rows = PaymentTable.deleteWhere {
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
                PaymentTable.select { amount eq 10.toBigDecimal() }
            targetAmount > 100.toBigDecimal() -> {
                PaymentTable.select { amount eq 20.toBigDecimal() }
            }
            else -> {
                PaymentTable.select { amount eq 30.toBigDecimal() }
            }
        }
    }

    @Test
    fun `join query`() {

        Database.connect(dataSource)
        transaction {
//            SchemaUtils.create(OrderTable)
//            SchemaUtils.create(PaymentTable)

            val selectAll = PaymentTable.join(OrderTable, JoinType.INNER, additionalConstraint = { OrderTable.id eq orderId })
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

object PaymentTable : LongIdTable(name = "payment") {
    val orderId = long("order_id")
    val amount = decimal("amount", 19, 4)
}

object OrderTable : LongIdTable(name = "orders") {
    val amount = decimal("amount", 19, 4)
    val name = varchar("name", 100)
}

class Payment(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Payment>(PaymentTable)

    var amount by PaymentTable.amount
    var orderId by PaymentTable.orderId

    override fun toString(): String {
        return "City(name='$amount')"
    }
}