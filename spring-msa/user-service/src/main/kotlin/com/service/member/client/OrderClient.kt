package com.service.member.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "order-service", url = "http://localhost:5555/order-service")
interface OrderClient {

    @GetMapping("/api/v1/orders/users/{userId}")
    fun getOrderByUserId(@PathVariable userId: String): List<OrderResponse>
}

data class OrderResponse(
    val productId: String,
    val userId: String,
    val orderId: String,
    val qty: Int,
    val unitPrice: Int,
    val totalPrice: Int
)