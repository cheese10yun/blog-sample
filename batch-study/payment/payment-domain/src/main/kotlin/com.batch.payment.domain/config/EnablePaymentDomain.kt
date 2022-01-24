package com.batch.payment.domain.config

import com.querydsl.jpa.impl.JPAQueryFactory
import javax.persistence.EntityManager
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.EnableTransactionManagement

const val basePackages = "com.batch.payment.domain"
const val paymentTransactionManager = "PAYMENT_TRANSACTION_MANAGER"

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ComponentScan(basePackages = [basePackages])
@EnableJpaRepositories(
    basePackages = [basePackages],
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
@EnableAutoConfiguration
@EnableTransactionManagement
@EntityScan(basePackages = [basePackages])
annotation class EnablePaymentDomain


