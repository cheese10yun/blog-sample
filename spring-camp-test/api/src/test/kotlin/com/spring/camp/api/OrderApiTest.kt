package com.spring.camp.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

class OrderApiTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
) : TestSupport() {


@Test
internal fun `주문 API TEST`() {
    //given
    val dto = OrderRequest(
        orderNumber = "A00001",
        status = "READY",
        price = 1000L,
        address = Address(
            zipCode = "023",
            address = "서울 중구 을지로 65",
            detail = "SK텔레콤빌딩 4층 수펙스홀"
        )
    )

//    val requestBody = objectMapper.writeValueAsString(dto)
    val requestBody = readJson("/order-1.json")


    //when & then
    mockMvc.post("/v1/orders") {
        contentType = MediaType.APPLICATION_JSON
        content = requestBody
    }.andExpect {
        status { isOk() }
    }
}

//    @Test
//    internal fun `test`() {
//        //given
//        val dto = OrderRequest(
//            orderNumber = "A00001",
//            status = "READY",
//            price = 1000L
//        )
//
//        val requestBody = objectMapper.writeValueAsString(dto)
//
//        //when & then
//        mockMvc.post("/v1/orders") {
//            contentType = MediaType.APPLICATION_JSON
//            content = requestBody
//        }.andExpect {
//            status { isOk() }
//            content {
//                contentType(MediaType.APPLICATION_JSON)
//                content {
//                    json(
//                        """
//                        {
//                          "order_number": "A00001",
//                          "status": "READY",
//                          "price": 1000
//                        }
//                    """.trimIndent()
//                    )
//                }
//            }
//        }.andDo {
//            print()
//        }
//    }
}