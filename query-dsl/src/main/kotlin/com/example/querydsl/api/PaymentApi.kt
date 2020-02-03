package com.example.querydsl.api

import com.example.querydsl.domain.Payment
import com.example.querydsl.repository.payment.PaymentRepositoryImpl
import com.example.querydsl.service.PaymentService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*

import java.math.BigDecimal

@RestController
@RequestMapping("/payment")
class PaymentApi(
    private val paymentRepositoryImpl: PaymentRepositoryImpl,
    private val paymentService: PaymentService
) {

    @GetMapping
    fun get(@RequestParam amount: BigDecimal, pageable: Pageable): Page<Payment> {
        return paymentRepositoryImpl.findBy(amount, pageable)
    }

    @PostMapping
    fun doPayment(@RequestBody dto: BankAccountPayment) {

        paymentService.doPayment(dto)

    }

    @GetMapping("/test")
    fun get2(): List<Payment> {
        return paymentRepositoryImpl.findByLimit(10)
    }

    data class BankAccountPayment(
        val bankAccount: String,
        val bankHolder: String,
        val bankCode: String,
        val amount: BigDecimal

    )
}