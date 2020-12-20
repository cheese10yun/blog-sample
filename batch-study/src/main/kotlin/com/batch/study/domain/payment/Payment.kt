package com.batch.study.domain.payment

import com.batch.study.core.LineAggregator
import com.batch.study.core.LineMapper
import com.batch.study.domain.EntityAuditing
import org.springframework.batch.item.file.transform.FieldSet
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

data class PaymentCsv(
    val amount: BigDecimal,
    val orderId: Long
) {
    fun toEntity() = Payment(amount, orderId)
}

class PaymentCsvMapper :
    LineMapper<PaymentCsv>,
    LineAggregator<PaymentCsv> {

    override val headerNames: Array<String> = arrayOf(
        "amount", "orderId"
    )

    override fun fieldSetMapper(fs: FieldSet) = PaymentCsv(
        amount = fs.readBigDecimal("amount"),
        orderId = fs.readLong("orderId")
    )
}