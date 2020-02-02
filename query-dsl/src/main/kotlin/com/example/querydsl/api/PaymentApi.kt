package com.example.querydsl.api

import com.example.querydsl.domain.Payment
import com.example.querydsl.repository.payment.PaymentRepositoryImpl
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import java.math.BigDecimal

@RestController
@RequestMapping("/payment")
class PaymentApi (
    private val paymentRepositoryImpl: PaymentRepositoryImpl
){

    @GetMapping
    fun get(@RequestParam amount: BigDecimal, pageable: Pageable): Page<Payment> {
        return paymentRepositoryImpl.findBy(amount, pageable)
    }

    @GetMapping("/test")
    fun get2(): List<Payment> {
        return paymentRepositoryImpl.findByLimit(10)
    }
}