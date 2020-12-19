package com.batch.study.domain.payment

import com.batch.study.domain.EntityAuditing
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "payment_back")
class PaymentBackJpa(
    @Column(name = "amount", nullable = false)
    var amount: BigDecimal,

    @Column(name = "order_id", nullable = false, updatable = false)
    val orderId: Long
){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        internal set
}