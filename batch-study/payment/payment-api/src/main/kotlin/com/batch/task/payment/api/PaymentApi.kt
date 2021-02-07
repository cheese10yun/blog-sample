package com.batch.task.payment.api

import com.batch.payment.domain.payment.PaymentRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/payemnts")
class PaymentApi(
    private val paymentRepository: PaymentRepository
) {

    @GetMapping
    fun getPayments() = paymentRepository.findAll()
}