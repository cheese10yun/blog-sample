package com.example.applicationevent.order

import javax.persistence.*

@Entity
@Table(name = "orders")
data class Order(

        @ElementCollection(fetch = FetchType.LAZY)
        @CollectionTable(name = "orders_item", joinColumns = [JoinColumn(name = "orders_id", nullable = false)])
        val orderItem: List<OrderItem>
) {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null
}