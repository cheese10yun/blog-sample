package com.batch.payment.domain.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

const val basePackages = "com.batch.payment.domain"

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ComponentScan(basePackages = [basePackages])
@EnableJpaRepositories(
    basePackages = [basePackages],
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager",

    )
@EntityScan(basePackages = [basePackages])
annotation class EnablePaymentDomain
