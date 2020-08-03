package com.example.querydsl.domain

import com.example.querydsl.repository.payment.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "payment")
data class Payment(

    @Column(name = "amount", nullable = false)
    var amount: BigDecimal

) : EntityAuditing() {
}

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository
) {

    fun aaaa() {

        bbbb()
    }

    @Transactional
    fun bbbb() {
        (1..10).map {
            paymentRepository.save(Payment(it.toBigDecimal()))
        }
    }
}