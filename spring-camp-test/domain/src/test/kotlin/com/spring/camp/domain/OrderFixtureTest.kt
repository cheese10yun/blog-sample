package com.spring.camp.domain

import java.time.LocalDate
import org.junit.jupiter.api.Test


class OrderFixtureTest(
    private val orderRepository: OrderRepository,
//    private val xxxService: XxxService
) {

    @Test
    fun `쿠폰 계산 테스트 코드`() {
        // given
        val customerName = "Willie Carlson"
        val status = "DICTAS"
        val orderDate = LocalDate.of(2024, 1, 2)
        val order = DomainFixTure.order(
            customerName = customerName,
            orderDate = orderDate,
            totalAmount = 8.9,
            status = status
        )
        val coupon = DomainFixTure.coupon(
            code = "COUPON_CODE",
            order = order
        )
//        save(order)
//        save(coupon)
//
//        // when
//        xxxService.xxxx(
//            order = order,
//            coupon = coupon
//        )
//        // then
//        then(...)
    }
}