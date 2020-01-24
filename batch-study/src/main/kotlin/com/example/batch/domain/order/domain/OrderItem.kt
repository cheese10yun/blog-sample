package com.example.batch.domain.order.domain

import javax.persistence.*

@Entity
@Table(name = "order_item")
data class OrderItem(

        @Column(name = "item_code", nullable = false)
        var itemCode: String,

        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        @JoinColumn(name = "order_id", nullable = false, updatable = false)
        val order: Order

) : EntityAuditing() {

}