//package com.spring.camp.domain
//
//import com.spring.camp.io.PartnerClient
//import java.time.LocalDate
//import org.junit.jupiter.api.Test
//import org.mockito.BDDMockito.given
//
//
//class OrderFixtureTest(
//    private val orderRepository: OrderRepository,
////    private val xxxService: XxxService
//) {
//
//    private val mockPartnerClient: PartnerClient
//
//    @Test
//    fun `쿠폰 계산 테스트 코드`() {
//        // given
////        val customerName = "Willie Carlson"
////        val status = "DICTAS"
////        val orderDate = LocalDate.of(2024, 1, 2)
////        val order = DomainFixTure.order(
////            customerName = customerName,
////            orderDate = orderDate,
////            totalAmount = 8.9,
////            status = status
////        )
////        val coupon = DomainFixTure.coupon(
////            code = "COUPON_CODE",
////            order = order
////        )
////        save(order)
////        save(coupon)
////
////        // when
////        xxxService.xxxx(
////            order = order,
////            coupon = coupon
////        )
////        // then
////        then(...)
//    }
//
////    @Test
////    fun `매월 1일 쿠폰 할인 적용 테스트`() {
////        // given
////        val order = DomainFixTure.order(
////            totalAmount = 1200.0,
////            orderDate = LocalDate.of(2024, 9, 1),
////        )
////        val coupon = DomainFixTure.coupon(discount = 1000.0)
////        ...
////        // when
////        xxxService(order = order, coupon = coupon)
////        ...
////    }
////
////    @Test
////    fun `주문 일자 기준 환율 주문 테스트`() {
////        // given
////        val orderDate1 = LocalDate.of(2024, 9, 1)
////        val orderDate2 = LocalDate.of(2024, 9, 2)
////        val orderDate3 = LocalDate.of(2024, 9, 3)
////        listOf(
////            DomainFixTure.order(
////                totalAmount = 1200.0,
////                orderDate = orderDate1,
////            ),
////            DomainFixTure.order(
////                totalAmount = 1200.0,
////                orderDate = orderDate2,
////            ),
////            DomainFixTure.order(
////                totalAmount = 1200.0,
////                orderDate = orderDate3,
////            )
////        )
////
////        mockServer
////            .expect(requestTo(".../api/v1/exchange-rate/USD-to-KRW/${orderDate1}"))
////            .andExpect(method(HttpMethod.GET))
////            .andRespond(
////                withStatus(HttpStatus.OK)
////                    .contentType(MediaType.APPLICATION_JSON)
////                    .body(
////                        """
////                        {
////                          "exchange_rate": "1,329.50"
////                        }
////                    """.trimIndent()
////                    )
////            )
////
////        mockServer
////            .expect(requestTo(".../api/v1/exchange-rate/USD-to-KRW/${orderDate2}"))
////            .andExpect(method(HttpMethod.GET))
////            .andRespond(
////                withStatus(HttpStatus.OK)
////                    .contentType(MediaType.APPLICATION_JSON)
////                    .body(
////                        """
////                        {
////                          "exchange_rate": "1,320.50"
////                        }
////                    """.trimIndent()
////                    )
////            )
////
////        mockServer
////            .expect(requestTo(".../api/v1/exchange-rate/USD-to-KRW/${orderDate3}"))
////            .andExpect(method(HttpMethod.GET))
////            .andRespond(
////                withStatus(HttpStatus.OK)
////                    .contentType(MediaType.APPLICATION_JSON)
////                    .body(
////                        """
////                        {
////                          "exchange_rate": "1,319.50"
////                        }
////                    """.trimIndent()
////                    )
////            )
////
////    }
//
//    @Test
//    fun `주문 일자 기준 환율 주문 테스트 `() {
//        // given
//        val orderDate1 = LocalDate.of(2024, 9, 1)
//        val orderDate2 = LocalDate.of(2024, 9, 2)
//        val orderDate3 = LocalDate.of(2024, 9, 3)
//        listOf(
//            DomainFixture.order(
//                totalAmount = 1200.0,
//                orderDate = orderDate1,
//            ),
//            DomainFixture.order(
//                totalAmount = 1200.0,
//                orderDate = orderDate2,
//            ),
//            DomainFixture.order(
//                totalAmount = 1200.0,
//                orderDate = orderDate3,
//            )
//        )
//
//        given(mockPartnerClient.getExchangeRate(orderDate1))
//            .willReturn(ExchangeRateResponse(exchangeRate = "1,329.50"))
//
//        given(mockPartnerClient.getExchangeRate(orderDate2))
//            .willReturn(ExchangeRateResponse(exchangeRate = "1,320.50"))
//
//        given(mockPartnerClient.getExchangeRate(orderDate3))
//            .willReturn(ExchangeRateResponse(exchangeRate = "1,319.50"))
//
//    }
//
////@Test
////fun `매월 1일 쿠폰 할인 적용 테스트` () {
////    // given
////    val order = DomainFixTure.order(
////        customerName = "홍길동",
////        orderDate = LocalDate.of(2024, 4, 4),
////        totalAmount = 8.9,
////        status = "READY",
////        shippingAddress = "서울 특별시 xxx 구, xxx로 123",
////        billingAddress = "서울 특별시 xxx 구, xxx로 123",
////        paymentMethod = "CARD",
////        shippingCost = 10.11,
////        taxAmount = 12.13,
////        discount = 14.15,
////        notes = null,
////    )
////    val coupon = DomainFixTure.coupon(discount = 1000.0)
////    ...
////
////    // when
////    xxxService(order = order, coupon = coupon)
////    ...
////}
//}
//
//data class ExchangeRateResponse(
//    val exchangeRate: String
//)