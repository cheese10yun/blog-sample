package com.example.batch.domain.order.dao

import com.example.batch.SpringBootTestSupport
import org.junit.Test


internal class OrderRepositoryTest(
    private  val orderRepository: OrderRepository
) : SpringBootTestSupport() {




    @Test
    fun test() {
        val orders = orderRepository.findAll()

        for (order in orders) {
            println(order)
        }

    }
}