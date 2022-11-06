package com.example.querydsl.domain

import com.example.querydsl.SpringBootTestSupport
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StopWatch
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.sql.DataSource

//@Transactional
internal class WriterTest(
    private val writerRepository: WriterRepository,
    private val writerService: WriterService,
    private val dataSource: DataSource,
) : SpringBootTestSupport() {

    /**
     * rows 50, batch_size 1,000, 337 ms
     * rows 100, batch_size 1,000, 327 ms
     * rows 500, batch_size 1,000, 908 ms
     * rows 1,000, batch_size 1,000, 1491 ms
     * rows 5,000, batch_size 1,000, 5,662 ms
     * rows 10,000, batch_size 1,000, 10927 ms
     * rows 50,000, batch_size 1,000, 51429 ms
     * rows 100,000, batch_size 10,000, 101595 ms
     */
    @Test
    internal fun `update test`() {
        val total = 500
        val map = (1..total).map {
            Writer(
                name = "123",
                email = "123@asd.com"
            )
        }

        setup(map)

        val stopWatch = StopWatch()
        stopWatch.start()
        writerService.update()
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