package com.batch.payment.domain.config

import org.springframework.context.annotation.ComponentScan

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ComponentScan(basePackages = ["com.batch.payment.domain"])
annotation class EnablePaymentDomain
