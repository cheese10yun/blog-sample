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