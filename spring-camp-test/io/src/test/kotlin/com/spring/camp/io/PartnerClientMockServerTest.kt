package com.spring.camp.io

import java.time.LocalDate
import kotlin.properties.Delegates
import org.assertj.core.api.BDDAssertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.*
import org.springframework.web.client.RestTemplate

class PartnerClientMockServerTest(
    private val partnerClient: PartnerClient,
    private val partnerClientRestTemplate: RestTemplate
) : TestSupport() {

    private var mockServer: MockRestServiceServer by Delegates.notNull()

    @BeforeEach
    internal fun setUp() {
        mockServer = MockRestServiceServer.createServer(
            this.partnerClientRestTemplate
        )
    }

    @Test
    fun `2xx가 아닌 경우 IllegalArgumentException 발생`() {
        //given
        val brn = "000-00-0000"
        val name = "주식회사 XXX"
        mockServer
            .expect(
                requestTo("http://localhost:8787/api/v1/partner/${brn}")
            )
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                        """
                            {
                              "brn": "$brn",
                              "name": "$name"
                            }
                        """.trimIndent()
                    )
            )

        //when & then
        thenThrownBy {
            partnerClient.getPartnerBy(brn)
        }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `주문일자 기준 `() {
        //given
        val orderDate = LocalDate.of(2024, 8, 2)
        mockServer
            .expect(requestTo("http://localhost:8787/api/v1/exchange-rate/USD-to-KRW/${orderDate}"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                        """
                            {
                              "exchange_rate": "1,369.50"
                            }
                        """.trimIndent()
                    )
            )

    }

    @Test
    fun `getPartnerBy test`() {
        //given
        val brn = "000-00-0000"
        val name = "주식회사 XXX"
        mockServer
            .expect(
                requestTo("http://localhost:8787/api/v1/partner/${brn}")
            )
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withStatus(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                        """
                            {
                              "brn": "$brn",
                              "name": "$name"
                            }
                        """.trimIndent()
                    )
            )

        //when & then
        val response = partnerClient.getPartnerBy(brn)
        then(response.brn).isEqualTo(brn)
//        then(response.name).isEqualTo("김밥천국")
    }
}