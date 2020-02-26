package com.example.querydsl.service

import com.example.querydsl.SpringBootTestSupport
import com.example.querydsl.domain.Payment
import com.example.querydsl.domain.QPayment
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class PaymentServiceTest : SpringBootTestSupport() {

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
    internal fun name() {

        saveAll((1..100).map {
            Payment(it.toBigDecimal().plus(BigDecimal.TEN).setScale(0))
        })


        query.selectFrom(QPayment.payment)

    }
}