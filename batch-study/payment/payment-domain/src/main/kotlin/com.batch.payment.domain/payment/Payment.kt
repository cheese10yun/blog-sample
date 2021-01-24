package com.batch.payment.domain.payment

import com.batch.payment.domain.core.EntityAuditing
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

) : EntityAuditing() {

    override fun toString(): String {
        return "Payment(amount=$amount, orderId=$orderId)"
    }
}

interface PaymentRepository : JpaRepository<Payment, Long>