package com.example.eventtransaction.cart

import com.example.eventtransaction.EntityAuditing
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "orders")
class Cart(
    @Column(name = "product_id", nullable = false)
    var productId: Long,

    @Column(name = "member_id", nullable = false, updatable = false)
    var memberId: Long
) : EntityAuditing()