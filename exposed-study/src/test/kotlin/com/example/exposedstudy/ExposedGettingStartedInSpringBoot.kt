package com.example.exposedstudy

import com.example.exposedstudy.Payments.amount
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import java.math.BigDecimal
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.junit.jupiter.api.Test


class ExposedGettingStartedInSpringBoot : ExposedTestSupport() {

    @Test
    fun `exposed DAO`() {
        // connection to MySQL
        // Database.connect(dataSource) 스프링 Bean의 DataSource를 사용하기 때문에 주석

        // transaction { 스프링 @Transactional 으로 트랜잭션을 시작하기 때문에 주석
            // Show SQL logging
            // addLogger(StdOutSqlLogger)  logging.level.Exposed: debug 으로 Show SQL logging 확인

            // CREATE TABLE IF NOT EXISTS payment (id BIGINT AUTO_INCREMENT PRIMARY KEY, order_id BIGINT NOT NULL, amount DECIMAL(19, 4) NOT NULL)
            //  SchemaUtils.create(Payments)  generate-ddl: true 으로 스키마 생성

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
            // SchemaUtils.drop(Payments)
        // }
    }

    @Test
    fun `exposed DSL`() {
        // connection to MySQL
        // Database.connect(dataSource) 스프링 Bean의 DataSource를 사용하기 때문에 주석

        // transaction { 스프링 @Transactional 으로 트랜잭션을 시작하기 때문에 주석
            // Show SQL logging
            // addLogger(StdOutSqlLogger)  logging.level.Exposed: debug 으로 Show SQL logging 확인

            // CREATE TABLE IF NOT EXISTS payment (id BIGINT AUTO_INCREMENT PRIMARY KEY, order_id BIGINT NOT NULL, amount DECIMAL(19, 4) NOT NULL)
            //  SchemaUtils.create(Payments)  generate-ddl: true 으로 스키마 생성

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
            // SchemaUtils.drop(Payments)
        // }
    }
}
