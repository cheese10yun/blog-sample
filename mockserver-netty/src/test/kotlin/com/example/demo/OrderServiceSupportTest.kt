package com.example.demo

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

internal class OrderServiceSupportTest {

    @Test
    internal fun `쿠폰 적용 없는 주문 생성`() {
        //given
        val product = Product(
            productId = ...,
            amount = ...,
            currency = ...,
        )
        val orderDate = LocalDate.of(2022, 2, 2)
        val orderAmount = 100.toBigDecimal()
        val exchangeRateResponse = ExchangeRateResponse(
            1222.12.toBigDecimal()
        )
        val shop = Shop(
            feeRate = 0.023.toBigDecimal()
        )

        //when
        val order = OrderServiceSupport().order(
            product = product,
            orderDate = orderDate,
            orderAmount = orderAmount,
            exchangeRateResponse = exchangeRateResponse,
            shop = shop,
            coupon = null,
        )

        //then
        // 복잡한 로직..에 대한 검증
    }
}