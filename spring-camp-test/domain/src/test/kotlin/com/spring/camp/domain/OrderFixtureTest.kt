package com.spring.camp.domain

import java.time.LocalDate
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test


class OrderFixtureTest(
    private val orderRepository: OrderRepository
) {

//    @Test
//    fun `조회 테스트`() {
//        // given
//        val customerName = "Willie Carlson"
//        val status = "DICTAS"
//        val orderDate = LocalDate.of(2024, 1, 2)
//        Order(
//            customerName = customerName,
//            orderDate = orderDate,
//            totalAmount = 8.9,
//            status = status,
//            shippingAddress = "sapien",
//            billingAddress = "ridens",
//            paymentMethod = "libero",
//            shippingCost = 10.11,
//            taxAmount = 12.13,
//            discount = 14.15,
//            notes = null
//        )
//
//        // when
//        val orders = orderRepository.findByCustomerNameAndStatusAndOrderDate(
//            customerName = customerName,
//            status = status,
//            orderDate = orderDate
//        )
//        // then
//        then(...)
//    }
}