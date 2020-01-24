package com.example.batch.domain.order.domain

import java.math.BigDecimal
import javax.persistence.*


@Entity
@Table(name = "orders")
data class Order(

        @Column(name = "amount", nullable = false)
        var amount: BigDecimal,


        @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL])
        val items: List<OrderItem> = listOf()


) : EntityAuditing() {

    fun updatePrice() {
        amount = BigDecimal("1209.11")
    }

}