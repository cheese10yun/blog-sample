package com.spring.camp.domain

import java.time.LocalDate

object DomainFixture {
    fun order(
        customerName: String = "홍길동",
        orderDate: LocalDate = LocalDate.of(2024, 4, 4),
        totalAmount: Double = 8.9,
        status: String = "READY",
        shippingAddress: String = "서울 특별시 xxx 구, xxx로 123",
        billingAddress: String = "서울 특별시 xxx 구, xxx로 123",
        paymentMethod: String = "CARD",
        shippingCost: Double = 10.11,
        taxAmount: Double = 12.13,
        discount: Double = 14.15,
        notes: String? = null,
    ): Order {
        return Order(
            customerName = customerName,
            orderDate = orderDate,
            totalAmount = totalAmount,
            status = status,
            shippingAddress = shippingAddress,
            billingAddress = billingAddress,
            paymentMethod = paymentMethod,
            shippingCost = shippingCost,
            taxAmount = taxAmount,
            discount = discount,
            notes = notes,
        )
    }

    fun coupon(
        code: String = "COUPON_CODE",
        discount: Double = 10.0,
//        order: Order
    ): Coupon {
        return Coupon(
            code = code,
            id = null
        )
    }
}

data class Coupon(
    val id: Long? = null,
    val code: String
)