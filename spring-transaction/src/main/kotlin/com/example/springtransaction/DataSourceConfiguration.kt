package com.example.springtransaction

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

const val PROPERTIES = "spring.datasource.hikari"
const val MASTER_DATASOURCE = "dataSource"
const val READER_DATASOURCE = "readerDataSource"

@Configuration
class DataSourceConfiguration {

    @Bean(MASTER_DATASOURCE)
    @Primary
    @ConfigurationProperties(prefix = PROPERTIES)
    fun dataSource() =
        DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .build()

    @Bean(READER_DATASOURCE)
    @ConfigurationProperties(prefix = PROPERTIES)
    fun readerDataSource() =
        DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .build()
            .apply { this.isReadOnly = true }

}