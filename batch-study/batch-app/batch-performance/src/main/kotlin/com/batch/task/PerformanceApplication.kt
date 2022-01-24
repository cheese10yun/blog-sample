package com.batch.task

import com.batch.payment.domain.config.EnablePaymentDomain
import com.querydsl.jpa.impl.JPAQueryFactory
import javax.persistence.EntityManager
import javax.sql.DataSource
import org.jetbrains.exposed.sql.Database
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableBatchProcessing
@EnablePaymentDomain
class PerformanceApplication

fun main(args: Array<String>) {
    runApplication<PerformanceApplication>(*args)
}

@Component
class ExposedConfig(
    private val dataSource: DataSource
) {

    @Bean
    fun exposedDataBase() =
        Database.connect(dataSource)
}

@Component
class QueryFactoryConfig {

    @Bean
    fun query(entityManager: EntityManager) =
        JPAQueryFactory(entityManager)
}