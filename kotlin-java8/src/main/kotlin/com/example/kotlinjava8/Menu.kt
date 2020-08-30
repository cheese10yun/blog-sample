package com.example.kotlinjava8

data class Menu(
    val name: String,
    val vegetarian: Boolean,
    val calories: Int,
    val type: Type
) {
}

enum class Type {
    MEAT,
    FISH,
    OTHER
}