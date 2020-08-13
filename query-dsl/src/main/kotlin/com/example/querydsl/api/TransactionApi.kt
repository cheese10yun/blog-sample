package com.example.querydsl.api

import com.example.querydsl.domain.Order
import com.example.querydsl.domain.Orderer
import com.example.querydsl.domain.Payment
import com.example.querydsl.repository.order.OrderRepository
import com.example.querydsl.repository.payment.PaymentRepository
import com.example.querydsl.service.Coupon
import com.example.querydsl.service.CouponRepository
import com.example.querydsl.service.CouponService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transaction")
class TransactionApi(
    private val couponService: CouponService,
    private val simpleService: SimpleService
) {

    @GetMapping
    fun transactional(@RequestParam i: Int) {
        couponService.something(i)
    }

    @GetMapping("/order")
    fun order() {
        simpleService.saveOrder()
    }
}

@Service
class SimpleService(
    private val couponRepository: CouponRepository,
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository,
    private val paymentSaveService: PaymentSaveService,
    private val couponSaveService: CouponSaveService
) {

    @Transactional
    fun saveOrder() {
        println("saveOrder CurrentTransactionName: ${TransactionSynchronizationManager.getCurrentTransactionName()}")
        orderRepository.save(Order(
            amount = 10.toBigDecimal(),
            orderer = Orderer(1L, "test@test.com")
        ))
//        this.savePayment()
//        this.saveCoupon()
        paymentSaveService.savePayment()
        couponSaveService.saveCoupon()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun savePayment() {
        println("savePayment CurrentTransactionName: ${TransactionSynchronizationManager.getCurrentTransactionName()}")
        paymentRepository.save(Payment(10.toBigDecimal()))
    }

    @Transactional
    fun saveCoupon() {
        println("saveCoupon CurrentTransactionName: ${TransactionSynchronizationManager.getCurrentTransactionName()}")
        couponRepository.save(Coupon(10.toBigDecimal()))
        throw RuntimeException()
    }
}

@Service
class PaymentSaveService(
    private val paymentRepository: PaymentRepository
){
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun savePayment() {
        println("savePayment CurrentTransactionName: ${TransactionSynchronizationManager.getCurrentTransactionName()}")
        paymentRepository.save(Payment(10.toBigDecimal()))
    }
}

@Service
class CouponSaveService(
    private val couponRepository: CouponRepository
){
    @Transactional
    fun saveCoupon() {
        println("saveCoupon CurrentTransactionName: ${TransactionSynchronizationManager.getCurrentTransactionName()}")
        couponRepository.save(Coupon(10.toBigDecimal()))
        throw RuntimeException()
    }
}