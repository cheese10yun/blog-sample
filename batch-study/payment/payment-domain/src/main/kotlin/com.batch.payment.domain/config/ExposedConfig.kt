package com.batch.payment.domain.config

import javax.sql.DataSource
import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.Database
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.EnableTransactionManagement

@Component
@EnableTransactionManagement
class ExposedConfig(
    private val dataSource: DataSource
) {

    @Bean
    fun exposedDataBase() = Database.connect(dataSource)

    @Bean
    fun springTransactionManager(dataSource: DataSource): SpringTransactionManager =
        SpringTransactionManager(dataSource)
}