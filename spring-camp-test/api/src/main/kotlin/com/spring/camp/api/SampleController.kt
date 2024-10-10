package com.spring.camp.api

import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import org.hibernate.internal.CoreLogging.logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sample")
class SampleController(
    private val dataSource: DataSource
) {
    private val log = LoggerFactory.getLogger(javaClass)!!

    @GetMapping
    fun sample() {
        val targetDataSource = (dataSource as LazyConnectionDataSourceProxy).targetDataSource

        // targetDataSource를 HikariDataSource로 캐스팅
        val hikariDataSource = targetDataSource as HikariDataSource
        val hikariPoolMXBean = hikariDataSource.hikariPoolMXBean
        val hikariConfigMXBean = hikariDataSource.hikariConfigMXBean
        val trimIndent =
            """
            totalConnections : ${hikariPoolMXBean.totalConnections}
            activeConnections : ${hikariPoolMXBean.activeConnections}
            idleConnections : ${hikariPoolMXBean.idleConnections}
            threadsAwaitingConnection : ${hikariPoolMXBean.threadsAwaitingConnection}
            maxLifetime : ${hikariConfigMXBean.maxLifetime}
            maximumPoolSize : ${hikariConfigMXBean.maximumPoolSize}
            connectionTimeout : ${hikariConfigMXBean.connectionTimeout}
            validationTimeout : ${hikariConfigMXBean.validationTimeout}
            idleTimeout : ${hikariConfigMXBean.idleTimeout}
            """.trimIndent()

        log.info(trimIndent)
    }
}