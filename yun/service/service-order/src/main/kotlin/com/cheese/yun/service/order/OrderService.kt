package com.cheese.yun.service.order

import com.cheese.yun.domain.model.Address
import com.cheese.yun.domain.order.Order
import com.cheese.yun.domain.order.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal


@Service
class OrderService(
    private val orderRepository: OrderRepository
) {
    @Transactional
    fun order(
        address: Address,
        price: BigDecimal
    ) {
        orderRepository.save(Order(address, price))
    }
}