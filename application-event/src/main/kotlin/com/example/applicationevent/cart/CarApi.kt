package com.example.applicationevent.cart

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/carts")
class CarApi(
        private val cartRepository: CartRepository
) {

    @GetMapping
    fun getCarts(): List<Cart> {
        return cartRepository.findAll()
    }

}