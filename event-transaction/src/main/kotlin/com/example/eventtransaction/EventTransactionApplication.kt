package com.example.eventtransaction

import com.example.eventtransaction.cart.Cart
import com.example.eventtransaction.cart.CartRepository
import com.example.eventtransaction.order.OrderRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableAsync
class EventTransactionApplication

fun main(args: Array<String>) {
    runApplication<EventTransactionApplication>(*args)
}

@Component
class EventTransactionApplicationRunner(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
//        orderRepository.save(Order(1000.toBigDecimal(), 1L, Orderer(1L, "test@test.com")))
        cartRepository.save(Cart(1L, 1L))
    }
}
