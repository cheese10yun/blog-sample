package com.example.webflux

import org.springframework.data.annotation.Id
import org.springframework.data.repository.reactive.ReactiveCrudRepository

class Item(
    var name: String,
    var price: Double
) {
    @Id
    var id: String? = null
}

interface ItemRepository : ReactiveCrudRepository<Item, String>

class CartItem(
    var item: Item,
) {
    var quantity: Int = 1
}

interface CartItemRepository : ReactiveCrudRepository<CartItem, String>

class Cart(
    @Id
    var id: String,
    var cartItems: List<CartItem> = emptyList()
) {

}

interface CartRepository : ReactiveCrudRepository<Cart, String>