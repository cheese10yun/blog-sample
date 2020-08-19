package com.service.cart

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CartApplication

fun main(args: Array<String>) {
    runApplication<CartApplication>(*args)
}