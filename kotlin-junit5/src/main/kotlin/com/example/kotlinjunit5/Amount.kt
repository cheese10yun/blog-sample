package com.example.kotlinjunit5

data class Amount(
        val price: Int,
        val ea: Int) {

    val totalPrice: Int
        get() = price * ea
}