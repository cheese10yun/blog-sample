package com.service.cart

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

@SpringBootApplication
class CartApplication

fun main(args: Array<String>) {
    runApplication<CartApplication>(*args)
}

@Component
class CartApplicationRunner(
    private val cartRepository: CartRepository
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {

        (1..10).map {
            cartRepository.save(Cart(it.toLong()))
        }
    }
}