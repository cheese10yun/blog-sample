package org.example.boot3webflux

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/orders")
class OrderController {

    @GetMapping
    fun getOrder(
        @RequestParam status: String,
        @RequestParam date: String,
    ): Mono<OrderResponse> {
        return Mono.just(
            OrderResponse(
                orderNumber = "1112230",
                status = status,
                date = date,
            )
        )
    }
}

@RestController
@RequestMapping("/api/v1/shops")
class ShopController {

    @GetMapping
    fun getShop(
        @RequestParam status: String,
        @RequestParam date: String,
    ): Mono<ShopResponse> {
        return Mono.just(
            ShopResponse(
                shopNumber = "1112230",
                status = status,
            )
        )
    }
}

data class OrderResponse(
    val orderNumber: String,
    val status: String,
    val date: String,
)

data class ShopResponse(
    val shopNumber: String,
    val status: String,
)