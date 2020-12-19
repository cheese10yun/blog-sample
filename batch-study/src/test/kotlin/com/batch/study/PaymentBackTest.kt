package com.batch.study

import com.batch.study.domain.payment.Payment
import com.batch.study.domain.payment.PaymentBack
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class PaymentBackTest {

    @Test
     fun asd() {

        val config = HikariConfig().apply {
            jdbcUrl =
                "jdbc:mysql://localhost:3366/batch_study?useSSL=false&serverTimezone=UTC&autoReconnect=true&rewriteBatchedStatements=true"
            driverClassName = "com.mysql.cj.jdbc.Driver"
            username = "root"
            password = ""
            maximumPoolSize = 20
        }


        val payments = (1..10)
            .map { Payment(it.toBigDecimal(), it.toLong()) }

        Database.connect(HikariDataSource(config))

        transaction {
            PaymentBack.batchInsert(payments) { payment ->
                this[PaymentBack.orderId] = payment.orderId
                this[PaymentBack.amount] = payment.amount
            }
        }


    }
}

