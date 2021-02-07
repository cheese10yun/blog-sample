package com.batch.task.payment.api

import com.batch.payment.domain.config.EnablePaymentDomain
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnablePaymentDomain
class PaymentApiApplication

fun main(args: Array<String>) {
    runApplication<PaymentApiApplication>(*args)
}