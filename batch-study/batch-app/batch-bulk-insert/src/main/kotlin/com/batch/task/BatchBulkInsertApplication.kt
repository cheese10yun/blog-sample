package com.batch.task

import com.batch.payment.domain.config.EnablePaymentDomain
import org.jetbrains.exposed.sql.Database
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import javax.sql.DataSource

@SpringBootApplication
@EnableBatchProcessing
@EnablePaymentDomain
class BatchBulkInsertApplication

fun main(args: Array<String>) {
    runApplication<BatchBulkInsertApplication>(*args)
}

@Component
class ExposedConfig(
    private val dataSource: DataSource
) {

    @Bean
    fun exposedDataBase() =
        Database.connect(dataSource)
}