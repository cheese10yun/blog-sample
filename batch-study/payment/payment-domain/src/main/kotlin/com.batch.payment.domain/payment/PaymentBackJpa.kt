package com.batch.payment.domain.payment

import com.batch.payment.domain.core.EntityAuditing
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "payment_back")
class PaymentBackJpa(
    @Column(name = "amount", nullable = false)
    var amount: BigDecimal,

    @Column(name = "order_id", nullable = false, updatable = false)
    val orderId: Long
) : EntityAuditing()