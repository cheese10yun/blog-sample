package com.kotlin.cookbook.step2

data class Person @JvmOverloads constructor(
    val first: String,
    val middle: String?,
    val last: String
) {
    @JvmOverloads
    fun addProduct(name: String, price: Int = 0, desc: String? = null) {
        println("name: $name")
        println("price: $price")
        println("String: $String")
    }
}