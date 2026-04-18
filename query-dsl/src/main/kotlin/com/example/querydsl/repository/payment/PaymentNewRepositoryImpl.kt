package com.example.querydsl.repository.payment


import com.example.querydsl.domain.Payment

import com.example.querydsl.repository.support.QuerydslNewRepositorySupport
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import com.example.querydsl.domain.QPayment.payment as qPayment

@Repository
class PaymentNewRepositoryImpl : QuerydslNewRepositorySupport(Payment::class.java) {

    fun findBy(amount: BigDecimal, pageable: Pageable): Page<Payment> {
        return applyPagination(
            pageable = pageable,
            contentQuery = {
                selectFrom(qPayment).where(qPayment.amount.gt(amount))
            },
            countQuery = {
                select(qPayment.count()).from(qPayment).where(qPayment.amount.gt(amount))
            }
        )
    }

    fun findByLimit(limit: Long): List<Payment> {
        return selectFrom(qPayment)
            .limit(limit)
            .fetch()
    }
}