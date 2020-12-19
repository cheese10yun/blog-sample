package com.batch.study.core

import com.zaxxer.hikari.HikariConfig
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ExposedDataSourceConfiguration(
    private val dataSourceProperties: DataSourceProperties
) {

    @Bean
    fun exposedDataSource() = HikariConfig().apply {
        jdbcUrl =
            "jdbc:mysql://localhost:3366/batch_study?useSSL=false&serverTimezone=UTC&autoReconnect=true&rewriteBatchedStatements=true"
        driverClassName = "com.mysql.cj.jdbc.Driver"
        username = "root"
        password = ""
        maximumPoolSize = 20
    }
}