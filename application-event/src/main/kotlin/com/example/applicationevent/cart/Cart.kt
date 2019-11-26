package com.example.applicationevent.cart

import javax.persistence.*

@Entity
@Table(name = "cart")
data class Cart(

        @Column(name = "item_code", nullable = false)
        val code: String

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}