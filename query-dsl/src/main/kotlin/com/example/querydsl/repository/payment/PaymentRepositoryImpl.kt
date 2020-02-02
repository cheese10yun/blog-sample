package com.example.querydsl.repository.payment


import com.example.querydsl.domain.Payment

import com.example.querydsl.repository.support.Querydsl4RepositorySupport
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.function.Function
import com.example.querydsl.domain.QPayment.payment as qPayment

@Repository
class PaymentRepositoryImpl : Querydsl4RepositorySupport(Payment::class.java) {

    fun findBy(amount: BigDecimal, pageable: Pageable): Page<Payment> {
        return applyPagination(pageable, Function {
            selectFrom(qPayment)
                .where(qPayment.amount.gt(amount))
        })
    }
}