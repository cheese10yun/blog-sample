package com.service.cart

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/carts")
class CartApi() {

    @GetMapping
    fun getCart() = Cart(1L)

}

class Cart(
    var memberId: Long
) {

}