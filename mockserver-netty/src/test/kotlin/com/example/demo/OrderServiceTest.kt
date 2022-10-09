package com.example.demo

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.Parameter
import java.time.LocalDate

internal class OrderServiceTest(
    private val orderService: OrderService,
    private val orderQueryService: OrderQueryService
) {


    private lateinit var mockServer: ClientAndServer

    @BeforeAll
    fun startServer() {
        this.mockServer = ClientAndServer.startClientAndServer(8080)
    }

    @AfterAll
    fun stopServer() {
        this.mockServer.stop()
    }

    @Test
    internal fun `환율 정보 기반으로 주문 금액 계산하여 주문 생성`() {
        //given
        val responseBody = """
                    {
                      "amount": "140000"
                    }
                """.trimIndent()


        mockServer.`when`(
            HttpRequest.request()
                .withMethod("GET")
                .withPath("/exchange-rate")
                .withPathParameters(
                    listOf(
                        Parameter.param("targetDate", "2022-02-02"),
                        Parameter.param("currencyForm", "USD"),
                        Parameter.param("currencyTo", "KRW")
                    )
                )

        ).respond(
            HttpResponse.response()
                .withBody(responseBody)
                .withStatusCode(200)
        )

        //when
        val orderNumber = orderService.order(1L, LocalDate.of(2022, 2, 2), 100.toBigDecimal())

        //then
        val findOrder = orderQueryService.findOrderNumber(orderNumber)
        then(findOrder.amount).isEqualByComparingTo(140000.toBigDecimal())
    }

    @Test
    internal fun `Mock 객체를 기반으로 테스트`() {
        //given

        //when
        val orderNumber = orderService.order(1L, LocalDate.of(2022, 2, 2), 100.toBigDecimal())

        //then
        val findOrder = orderQueryService.findOrderNumber(orderNumber)
        then(findOrder.amount).isEqualByComparingTo(140000.toBigDecimal())
    }
}