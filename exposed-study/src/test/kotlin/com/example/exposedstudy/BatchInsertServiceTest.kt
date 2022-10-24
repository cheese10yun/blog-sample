package com.example.exposedstudy

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test

internal class BatchInsertServiceTest(
    private val batchInsertService: BatchInsertService,
    private val dataSource: HikariDataSource
) : ExposedTestSupport() {

    @Test
    fun `spring transaction memory test`() {
        val ids = (1..2000).map { it }
        (1..2000).map {
            batchInsertService.batch(ids)
        }
    }
}


class AAA {

    val config = HikariConfig().apply {
        jdbcUrl = "jdbc:mysql://yun-cheese-docker.ay1.krane.9rum.cc:43306/voyager_74?useSSL=false&serverTimezone=UTC&autoReconnect=true&rewriteBatchedStatements=true"
        driverClassName = "com.mysql.cj.jdbc.Driver"
        username = "root"
        password = "test"
    }

    val datasource = HikariDataSource(config)

    object YunTest : LongIdTable("test") {
        val type1 = varchar("type1", 255).nullable()
        val valueOfSupply = varchar("value_of", 255)
    }

    @Test
    fun `bulk insert test`() {

        val map = (1..20)
            .map { it }
            .toList()


        val connect = Database.connect(datasource)

        transaction(connect) {


            SchemaUtils.drop(YunTest)
            SchemaUtils.create(YunTest)

            YunTest.batchInsert(
                map
            ) {
                this[YunTest.type1] = "type1"
                this[YunTest.valueOfSupply] = "valueOfSupply"
            }
        }
    }
}