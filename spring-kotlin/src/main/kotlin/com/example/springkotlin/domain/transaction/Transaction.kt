package com.example.springkotlin.domain.transaction

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(
    name = "transaction",
    uniqueConstraints = [UniqueConstraint(columnNames = ["payment_method_type", "partner_transaction_id"])]
)
class Transaction protected constructor() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
        protected set

    @Column(name = "code", nullable = false, updatable = false, unique = true)
    lateinit var code: String
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method_type", nullable = false, updatable = false)
    lateinit var paymentMethodType: PaymentMethodType
        protected set

    @Column(name = "partner_transaction_id", nullable = false, updatable = false)
    lateinit var partnerTransactionId: String
        protected set

//    @CreatedDate
//    @Column(name = "created_at", nullable = false, updatable = false)
//    lateinit var createdAt: LocalDateTime
//        protected set
//
//    @LastModifiedDate
//    @Column(name = "updated_at", nullable = false)
//    lateinit var updatedAt: LocalDateTime
//        protected set

    private constructor(
        code: String,
        paymentMethodType: PaymentMethodType,
        thirdPartyTransactionId: String
    ) : this() {
        this.code = code
        this.paymentMethodType = paymentMethodType
        this.partnerTransactionId = thirdPartyTransactionId
    }

    companion object {
        fun newInstance(
            code: String,
            paymentMethodType: PaymentMethodType,
            thirdPartyTransactionId: String
        ): Transaction {
            return Transaction(code, paymentMethodType, thirdPartyTransactionId)
        }
    }

}