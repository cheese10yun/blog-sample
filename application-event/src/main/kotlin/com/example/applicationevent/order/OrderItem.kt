package com.example.applicationevent.order

import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class OrderItem(

        @Column(name = "item_code", nullable = false)
        val code: String,

        @Column(name = "price", nullable = false)
        val price: BigDecimal,

        @Column(name = "name", nullable = false)
        val name: String

) {
}