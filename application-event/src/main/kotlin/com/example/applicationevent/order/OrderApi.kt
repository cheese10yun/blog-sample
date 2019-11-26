package com.example.applicationevent.order

import com.example.applicationevent.item.Item
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/orders")
class OrderApi(
        private val orderRepository: OrderRepository,
        private val orderService: OrderService
) {

    @GetMapping
    fun getOrders(): MutableList<Order> {
        return orderRepository.findAll()
    }

    @PostMapping
    fun ordering(@RequestBody itemCode: List<String>): Order {
        return orderService.order(itemCode)
    }


}