package com.example.exposedstudy

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.InsertStatement
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
                    Writers.insert {


                    }


                    val insertWriter = insertWriter("asd", "asd")
                    this[Books.writer] = insertWriter[Writers.id]
                    this[Books.title] = "$it-title"
                    this[Books.status] = BookStatus.NONE
                    this[Books.price] = it.toBigDecimal()
                    this[Books.createdAt] = LocalDateTime.now()
                    this[Books.updatedAt] = LocalDateTime.now()
                }

            }
        }
    }

    fun insertWriter(
        name: String,
        email: String
    ): InsertStatement<Number> {

        return Writers.insert { writer ->
            writer[this.name] = name
            writer[this.email] = email
            writer[this.createdAt] = LocalDateTime.now()
            writer[this.updatedAt] = LocalDateTime.now()
        }
    }

}
