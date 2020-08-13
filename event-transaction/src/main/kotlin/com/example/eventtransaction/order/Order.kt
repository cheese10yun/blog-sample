package com.example.eventtransaction.order

import com.example.eventtransaction.EntityAuditing
import java.math.BigDecimal
import javax.persistence.*

@Entity
@Table(name = "orders")
class Order(
    @Column(name = "product_amount", nullable = false)
    var productAmount: BigDecimal,

    @Column(name = "product_id", nullable = false)
    var productId: Long,

    @Embedded
    var orderer: Orderer
) : EntityAuditing()

@Embeddable
data class Orderer(
    @Column(name = "member_id", nullable = false, updatable = false)
    var memberId: Long,

    @Column(name = "email", nullable = false, updatable = false)
    var email: String
)