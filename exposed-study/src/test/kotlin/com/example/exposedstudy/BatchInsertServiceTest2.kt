package com.example.exposedstudy

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test

internal class BatchInsertServiceTest2(){

    private val config = HikariConfig().apply {
        jdbcUrl = "jdbc:mysql://localhost:3366/exposed_study?useSSL=false&serverTimezone=UTC&autoReconnect=true&rewriteBatchedStatements=true"
        driverClassName = "com.mysql.cj.jdbc.Driver"
        username = "root"
        password = ""
        maximumPoolSize = 12
    }




    @Test
    fun `datasource`() {
        val dataSource = HikariDataSource(config)
        transaction(Database.connect(dataSource)) {
            // Show SQL logging
            val ids = (1..2000).map { it }
            (1..2000).map {
                Books.batchInsert(
                        data = ids,
                        ignore = false,
                        shouldReturnGeneratedValues = false
                ) {
                    this[Books.writer] = 1L
                    this[Books.title] = "$it-title"
                    this[Books.price] = it.toBigDecimal()
                    this[Books.createdAt] = LocalDateTime.now()
                    this[Books.updatedAt] = LocalDateTime.now()
                }

            }
        }
    }
}
