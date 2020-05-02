package com.cheese.yun.api

import com.cheese.yun.domain.model.Address
import com.cheese.yun.domain.order.Order
import com.cheese.yun.domain.order.OrderRepository
import com.cheese.yun.service.order.OrderService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("api/orders")
class OrderApi(
    private val orderRepository: OrderRepository,
    private val orderService: OrderService
) {

    @PostMapping
    fun order(@RequestBody dto: OrderRequest) {
        orderRepository.save(Order(dto.address, dto.price))
    }

    @PostMapping("/2")
    fun order2(@RequestBody dto: OrderRequest){
        orderRepository.save(Order(dto.address, dto.price))
    }


}

data class OrderRequest(
    val address: Address,
    val price: BigDecimal
)