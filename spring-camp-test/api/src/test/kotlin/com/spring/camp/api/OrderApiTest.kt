package com.spring.camp.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.spring.camp.io.PartnerClient
import com.spring.camp.io.PartnerStatus
import com.spring.camp.io.PartnerStatusResponse
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
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

    @Test
    fun ㅁㄴㅇㄴㅁㅇ() {

        var asd: String? = ""
        asd = null

        val toString = asd.toString()

        println(toString)


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

@SpringBootTest
@AutoConfigureMockMvc
class xxxControllerTest(
    private val mockMvc: MockMvc,
    private val mockPartnerClient: PartnerClient,
) {

    @BeforeEach
    fun resetMock() {
        Mockito.reset(mockPartnerClient)
    }

    @Test
    internal fun `xx 등록 API 테스트`() {
        //given
        val brn = "000-00-0000"
        given(mockPartnerClient.getPartnerStatus(brn))
            .willReturn(
                PartnerStatusResponse(
                    status = PartnerStatus.OPEN,
                    closeBusinessDate = null
                )
            )

        //when & then
        mockMvc.post("/v1/xxx") {
            contentType = MediaType.APPLICATION_JSON
            content = "..."
        }.andExpect {
            status { isOk() }
        }
    }
}