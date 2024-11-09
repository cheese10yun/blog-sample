package com.example.redis

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class RedisConnectionPoolSample(
    private val couponRepository: CouponRepository,
    private val orderRepository: OrderRepository
) {

    fun get(): Pair<Coupon?, Order?> {
        val coupon = couponRepository.findByIdOrNull("1")
        val order = orderRepository.findByIdOrNull(1L)
        return Pair(coupon, order)
    }
}