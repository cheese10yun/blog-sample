package com.example.querydsl.api

import com.example.querydsl.domain.Writer
import com.example.querydsl.domain.WriterService
import org.springframework.util.StopWatch
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.sql.DataSource

@RestController
@RequestMapping("/api/writers")
class WriterApi(
    private val writerService: WriterService,
    private val dataSource: DataSource,
) {


    @GetMapping
    fun update() {
        val stopWatch = StopWatch()
        val total = 50_000

        val map = (1..total).map {
            Writer(
                name = "old",
                email = "old"
            )
        }
        setup(map)
        val writers = writerService.findAll()

        stopWatch.start()
        writerService.update(writers)
        stopWatch.stop()
        println("${writers.size}, ${stopWatch.lastTaskTimeMillis}")

    }

    @GetMapping("/none")
    fun updateNonePersist() {

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