package com.spring.camp.api

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/v1/orders")
class OrderApi {

    @PostMapping
    fun order(
        @RequestBody dto: OrderRequest,
    ): OrderRequest {
        return dto
    }
}

data class OrderRequest(
    val orderNumber: String,
    val status: String,
    val price: Long,
    val address: Address
)

data class Address(
    val zipCode: String,
    val address: String,
    val detail: String,
)