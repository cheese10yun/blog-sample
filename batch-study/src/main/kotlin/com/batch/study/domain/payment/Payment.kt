package com.batch.study.domain.payment

import com.batch.study.domain.EntityAuditing
import org.springframework.data.jpa.repository.JpaRepository
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "payment")
class Payment(
    @Column(name = "amount", nullable = false)
    var amount: BigDecimal,

    @Column(name = "order_id", nullable = false, updatable = false)
    val orderId: Long

) : EntityAuditing()

interface PaymentRepository : JpaRepository<Payment, Long>