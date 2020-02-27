package com.example.querydsl.service

import com.example.querydsl.SpringBootTestSupport
import com.example.querydsl.domain.Payment
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import com.example.querydsl.domain.QPayment.payment as qPayment

internal class PaymentServiceTest(
    private val paymentService: PaymentService
) : SpringBootTestSupport() {

    @BeforeEach
    internal fun setUp() {
        deleteAll(qPayment)
    }

    @Test
    internal fun `payment test`() {
        //given
        val payment = save(Payment(BigDecimal.TEN))

        //when
        payment.amount = payment.amount.plus(10.toBigDecimal()).setScale(0)

        //then
        then(payment.amount).isEqualTo(20.toBigDecimal())
    }

    @Test
    internal fun `paymentZero test`() {
        //given
        val targetAmount = 105.toBigDecimal()
        saveAll((1..100).map {
            Payment(it.toBigDecimal().plus(BigDecimal.TEN).setScale(0))
        })

        //when
        paymentService.paymentZero(targetAmount)

        //then
        val count = query
            .selectFrom(qPayment)
            .where(qPayment.amount.gt(targetAmount))
            .fetchCount()

        then(count).isEqualTo(0)
    }
}