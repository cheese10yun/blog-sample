//package com.example.querydsl.service
//
//import com.example.querydsl.SpringBootTestSupport
//import com.example.querydsl.domain.Payment
//import com.example.querydsl.domain.PaymentService
//import org.assertj.core.api.BDDAssertions.then
//import org.junit.jupiter.api.Test
//import java.math.BigDecimal
//import com.example.querydsl.domain.QPayment.payment as qPayment
//
//internal class PaymentServiceTest(
//    private val paymentService: PaymentService
//) : SpringBootTestSupport() {
//
//    @Test
//    internal fun `payment test`() {
//        //given
//        val payment = save(Payment(BigDecimal.TEN))
//
//        //when
//        payment.amount = payment.amount.plus(10.toBigDecimal()).setScale(0)
//
//        //then
//        then(payment.amount).isEqualTo(20.toBigDecimal())
//    }
//
//    @Test
//    internal fun `paymentZero test`() {
//        //given
//        val targetAmount = 105.toBigDecimal()
//        saveAll((1..100).map {
//            Payment(it.toBigDecimal().plus(BigDecimal.TEN).setScale(0))
//        })
//
//        //when
//        paymentService.paymentZero(targetAmount)
//
//        //then
//        val count = query
//            .selectFrom(qPayment)
//            .where(qPayment.amount.gt(targetAmount))
//            .fetchCount()
//
//        then(count).isEqualTo(0)
//    }
//
//    @Test
//    internal fun `save test`() {
//        val amount = 1209.toBigDecimal()
//        val payment = paymentService.save(amount)
//
//        then(payment.amount).isEqualTo(amount)
//
//        val findPayment = query.selectFrom(qPayment)
//            .where(qPayment.id.eq(payment.id))
//            .fetchOne()!!
//
//        then(findPayment.amount).isEqualByComparingTo(amount)
//    }
//}