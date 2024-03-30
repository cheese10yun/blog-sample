package org.example.boot3

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/orders")
class OrderController {

    @GetMapping
    fun hello(
        @RequestParam status: String,
        @RequestParam date: String,
    ): OrderResponse {
        return OrderResponse(
            orderNumber = "1112230",
            status = status,
            date = date,
        )
    }
}

data class OrderResponse(
    val orderNumber: String,
    val status: String,
    val date: String,
)