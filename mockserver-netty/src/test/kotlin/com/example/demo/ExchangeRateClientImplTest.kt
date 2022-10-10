package com.example.demo

import org.assertj.core.api.BDDAssertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.Parameter
import java.time.LocalDate

class ExchangeRateClientImplTest {

//    private lateinit var mockServer: ClientAndServer
//
//    @BeforeAll
//    fun startServer() {
//        this.mockServer = ClientAndServer.startClientAndServer(8080)
//    }
//
//    @AfterAll
//    fun stopServer() {
//        this.mockServer.stop()
//    }
//
//    @Test
//     fun `환율 정보 HTTP 통신 테스트`() {
//        //given
//        val responseBody = """
//                    {
//                      "amount": "140000"
//                    }
//                """.trimIndent()
//
//        mockServer.`when`(
//            HttpRequest.request()
//                .withMethod("GET")
//                .withPath("/exchange-rate")
//                .withPathParameters(
//                    listOf(
//                        Parameter.param("targetDate", "2022-02-02"),
//                        Parameter.param("currencyForm", "USD"),
//                        Parameter.param("currencyTo", "KRW")
//                    )
//                )
//
//        ).respond(
//            HttpResponse.response()
//                .withBody(responseBody)
//                .withStatusCode(200)
//        )
//
//        //when
//
//        val clientImpl = ExchangeRateClientImpl()
//        clientImpl.getExchangeRate(
//            targetDate = LocalDate.of(2022, 2, 2),
//            currencyForm = "USD",
//            currencyTo = "KRW",
//        )
//
//    }

    @Test
    fun asdasd() {

        val a = 201 / 100
        val b = 404 / 100

        println(a)
        println(b)


    }
}