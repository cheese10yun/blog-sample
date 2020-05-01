package com.cheese.yun.api

import com.cheese.yun.domain.model.Address
import com.cheese.yun.domain.order.Order
import com.cheese.yun.domain.order.OrderRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("api/orders")
class OrderApi(
    private val orderRepository: OrderRepository
) {

    @PostMapping
    fun order(@RequestBody dto: OrderRequest) {
        orderRepository.save(Order(dto.address, dto.price))
    }
}

data class OrderRequest(
    val address: Address,
    val price: BigDecimal
)