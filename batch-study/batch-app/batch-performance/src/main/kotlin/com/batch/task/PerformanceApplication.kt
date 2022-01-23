package com.batch.task

import com.batch.payment.domain.config.EnablePaymentDomain
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableBatchProcessing
@EnablePaymentDomain
class PerformanceApplication

fun main(args: Array<String>) {
    runApplication<PerformanceApplication>(*args)
}