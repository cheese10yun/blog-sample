package com.example.querydsl.repository.payment

import com.example.querydsl.domain.Payment
import com.example.querydsl.repository.support.QuerydslCusomRepositorySupport
import org.springframework.data.jpa.repository.JpaRepository
import java.math.BigDecimal
import com.example.querydsl.domain.QPayment.payment as qPayment

interface PaymentRepository : JpaRepository<Payment, Long>, PaymentCustomRepository {
}

interface PaymentCustomRepository {
    fun findUseSelectForm(targetAmount: BigDecimal): List<Payment>
    fun findUseSelect(targetAmount: BigDecimal): Long
    fun findUseForm(targetAmount: BigDecimal): List<Payment>?
}

class PaymentCustomRepositoryImpl : QuerydslCusomRepositorySupport(Payment::class.java), PaymentCustomRepository {

    override fun findUseForm(targetAmount: BigDecimal): List<Payment>? {
        return from(qPayment)
            .where(qPayment.amount.gt(targetAmount))
            .fetch()
    }


    override fun findUseSelectForm(targetAmount: BigDecimal): List<Payment> {
        return selectFrom(qPayment)
            .where(qPayment.amount.gt(targetAmount))
            .fetch()
    }

    override fun findUseSelect(targetAmount: BigDecimal): Long {
        return select(qPayment.id)
            .from(qPayment)
            .where(qPayment.amount.gt(targetAmount))
            .fetchCount()
    }
}