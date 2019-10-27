package com.example.jpanplus1.member

import com.example.jpanplus1.copon.Coupon
import com.example.jpanplus1.order.Order
import sun.rmi.runtime.Log
import java.time.LocalDateTime

data class MemberDto(
        val id: Long,
        val email: String,
        val name: String,
        val orders: List<Order>,
        val coupons: List<Coupon>,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
)

data class CouponDto(
        val id: Log,
        val code: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
)

data class OrderDto(
        val id: Long,
        val number: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
)