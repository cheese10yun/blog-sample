package com.example.querydsl.repository.payment

import com.example.querydsl.SpringBootTestSupport
import org.assertj.core.api.BDDAssertions
import org.junit.jupiter.api.Test

internal class PaymentRepositoryTest(
    private val paymentRepository: PaymentRepository
) : SpringBootTestSupport() {

    @Test
    internal fun `findUseForm`() {
        //given
        val targetAmount = 200.toBigDecimal()

        //when
        val payments = paymentRepository.findUseFrom(targetAmount)

        //then
        BDDAssertions.then(payments).anySatisfy {
            BDDAssertions.then(it.amount).isGreaterThan(targetAmount)
        }
    }

    @Test
    internal fun `findUseSelectForm`() {
        //given
        val targetAmount = 200.toBigDecimal()

        //when
        val payments = paymentRepository.findUseSelectFrom(targetAmount)

        //then
        BDDAssertions.then(payments).anySatisfy {
            BDDAssertions.then(it.amount).isGreaterThan(targetAmount)
        }
    }

    @Test
    internal fun `findUseSelect`() {
        //given
        val targetAmount = 200.toBigDecimal()

        //when
        val ids = paymentRepository.findUseSelect(targetAmount)

        //then
        BDDAssertions.then(ids).hasSizeGreaterThan(1)
    }
}