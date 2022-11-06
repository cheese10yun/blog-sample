package com.example.exposedstudy

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.statements.BatchUpdateStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.update
import org.junit.jupiter.api.Test
import org.springframework.util.StopWatch

class BatchInsertServiceTest(
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


    @Test
    fun `bulk update`() {
        // 50 23 ms
        // 100 40 ms
        // 500 135 ms
        // 1_000 381 ms
        // 5_000 1127 ms
        // 10_000 2227 ms
        // 50_000 10355 ms
        // 100_000 21370 ms
        val totalCount = 500
        val ids = (1..totalCount).map { it.toLong() }

        setup(ids, "old")

        val stopWatch = StopWatch()
        stopWatch.start()

        BatchUpdateStatement(Writers).apply {
            ids.forEach {
                addBatch(EntityID(it, Writers))
                this[Writers.email] = "update"
            }
        }
            .execute(TransactionManager.current())

        stopWatch.stop()

        result(ids, stopWatch)
    }

    @Test
    fun `update`() {
        // 50 80 ms
        // 100 130 ms
        // 500 596 ms
        // 1_000 1130 ms
        // 5_000 5121 ms
        // 10_000 10094 ms
        // 50_000 46506 ms
        // 100_000 99349 ms
        val totalCount = 500
        val ids = (1..totalCount).map { it.toLong() }

        setup(ids, "old")

        val stopWatch = StopWatch()
        stopWatch.start()
        for (writerId in ids) {
            Writers
                .update({ Writers.id eq writerId })
                {
                    it[email] = "update"
                }
        }
        stopWatch.stop()

        result(ids, stopWatch)
    }

    private fun setup(ids: List<Long>, name: String) {
        ids
            .chunked(50_000)
            .forEach {
                Writers.batchInsert(
                    data = it,
                    ignore = false,
                    shouldReturnGeneratedValues = false
                ) {
                    this[Writers.email] = name
                    this[Writers.name] = name
                }
            }
    }

    private fun result(ids: List<Long>, stopWatch: StopWatch) {
        println("================")
        println("${ids.size} update, ${stopWatch.lastTaskTimeMillis}")
        println("================")
    }
}