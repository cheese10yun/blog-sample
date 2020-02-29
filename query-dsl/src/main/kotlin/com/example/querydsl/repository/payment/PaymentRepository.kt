package com.example.querydsl.repository.payment

import com.example.querydsl.domain.Payment
import com.example.querydsl.repository.support.QuerydslCustomRepositorySupport
import org.springframework.data.jpa.repository.JpaRepository
import java.math.BigDecimal
import com.example.querydsl.domain.QPayment.payment as qPayment

interface PaymentRepository : JpaRepository<Payment, Long>, PaymentCustomRepository {
}

interface PaymentCustomRepository {
    fun findUseSelectFrom(targetAmount: BigDecimal): List<Payment>
    fun findUseSelect(targetAmount: BigDecimal): List<Long>
    fun findUseFrom(targetAmount: BigDecimal): List<Payment>
}

class PaymentCustomRepositoryImpl : QuerydslCustomRepositorySupport(Payment::class.java), PaymentCustomRepository {

    override fun findUseFrom(targetAmount: BigDecimal): List<Payment> {
        return from(qPayment)
            .where(qPayment.amount.gt(targetAmount))
            .fetch()
    }

    override fun findUseSelectFrom(targetAmount: BigDecimal): List<Payment> {
        return selectFrom(qPayment)
            .where(qPayment.amount.gt(targetAmount))
            .fetch()
    }

    override fun findUseSelect(targetAmount: BigDecimal): List<Long> {
        return select(qPayment.id)
            .from(qPayment)
            .where(qPayment.amount.gt(targetAmount))
            .fetch()
    }
}