package com.example.applicationevent.item

import java.math.BigDecimal
import javax.persistence.*

@Entity
@Table(name = "item")
data class Item(
        @Column(name = "item_code", nullable = false)
        val code: String,

        @Column(name = "price", nullable = false)
        val price: BigDecimal,

        @Column(name = "name", nullable = false)
        val name: String
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}