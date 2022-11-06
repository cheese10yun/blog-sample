package com.example.querydsl.domain

import com.example.querydsl.SpringBootTestSupport
import org.junit.jupiter.api.Test
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StopWatch
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.sql.DataSource

//@Transactional
@Rollback(false)
internal class WriterTest(
    private val writerRepository: WriterRepository,
    private val writerService: WriterService,
    private val dataSource: DataSource,
) : SpringBootTestSupport() {

    /**
     * rows 50, 0 ms
     * rows 100, 0 ms
     * rows 500, 10 ms
     * rows 1,000, 1491 ms
     * rows 5,000, 5,662 ms
     * rows 10,000, 10927 ms
     * rows 50,000, 51429 ms
     * rows 100,000, 1196 ms
     */
    @Test
    internal fun `dirty checking update test`() {
        // 업데이트 대상 rows, 50, 100, 500, 1,000, 5,000, 10,000, 50,000, 100,000
        val total = 500
        val map = (1..total).map {
            Writer(
                name = "old",
                email = "old"
            )
        }
        // 데이터 셋업, 속도 측정 포함 X
        setup(map)
        // 데이터 조회, 속도 특정 X
        val writers = writerService.findAll()

        val stopWatch = StopWatch()
        // 업데이트 속도 측정
        stopWatch.start()
        writerService.update(writers)
        stopWatch.stop()

        println("${map.size}, ${stopWatch.lastTaskTimeMillis}")
    }

    /**
     * rows 50, 167 ms
     * rows 100, 242 ms
     * rows 500, 994 ms
     * rows 1,000, 1540 ms
     * rows 5,000, 6441 ms
     * rows 10,000, 12209 ms
     * rows 50,000, 56295 ms
     * rows 100,000, 113194 ms
     */
    @Test
    internal fun `none persist context update test`() {
        // 업데이트 대상 rows, 50, 100, 500, 1,000, 5,000, 10,000, 50,000, 100,000
        val total = 500
        val map = (1..total).map {
            Writer(
                name = "old",
                email = "old"
            )
        }
        // 데이터 셋업, 속도 측정 포함 X
        setup(map)
        val findAll = writerService.findAll()

        // 업데이트 속도 측정
        val stopWatch = StopWatch()
        stopWatch.start()
        writerService.nonPersistContestUpdate(findAll.map { it.id!! })
        stopWatch.stop()

        println("${map.size}, ${stopWatch.lastTaskTimeMillis}")

    }


    internal fun setup(writers: List<Writer>) {
        val sql = "insert into writer (`name`, `email`, `created_at`, `updated_at`) values (?, ?, ?, ?)"
        val connection = dataSource.connection
        val statement = connection.prepareStatement(sql)!!
        val valueOf = Timestamp.valueOf(LocalDateTime.now())

        try {
            for (writer in writers) {
                statement.apply {
                    this.setString(1, "old")
                    this.setString(2, "old")
                    this.setTimestamp(3, valueOf)
                    this.setTimestamp(4, valueOf)
                    this.addBatch()
                }
            }
            statement.executeBatch()
        } catch (e: Exception) {
            throw e
        } finally {
            if (statement.isClosed.not()) {
                statement.close()
            }
            if (connection.isClosed.not()) {
                connection.close()
            }
        }
    }

}