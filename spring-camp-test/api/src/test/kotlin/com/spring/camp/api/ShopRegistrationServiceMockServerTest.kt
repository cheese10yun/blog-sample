package com.spring.camp.api

import kotlin.properties.Delegates
import org.assertj.core.api.BDDAssertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.*
import org.springframework.web.client.RestTemplate

class ShopRegistrationServiceMockServerTest(
    private val shopRegistrationService: ShopRegistrationService,
) : TestSupport() {

    private var mockServer: MockRestServiceServer by Delegates.notNull()

    @BeforeEach
    internal fun setUp() {
        mockServer = MockRestServiceServer.createServer(
            RestTemplateBuilder()
                .rootUri("http://localhost:8080")
                .build()
        )
    }

    @Test
    fun `가맹점 등록 Mock HTTP Test`() {
        //given
        val brn = "000-00-0000"
        val name = "주식회사 XXX"
        mockServer
            .expect(
                requestTo("http://localhost:8080/api/v1/partner/${brn}")
            )
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                        """
                            {
                              "brn": "${brn}",
                              "name": "${name}"
                            }
                        """.trimIndent()
                    )
            )

        //when
        val shop = shopRegistrationService.register(brn)

        mockServer.verify()

        //then
        then(shop.name).isEqualTo(name)
        then(shop.brn).isEqualTo(brn)
    }
}