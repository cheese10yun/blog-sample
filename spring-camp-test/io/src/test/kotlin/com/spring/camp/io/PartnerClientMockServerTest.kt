package com.spring.camp.io

import kotlin.properties.Delegates
import org.assertj.core.api.BDDAssertions.then
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
                withStatus(HttpStatus.OK)
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

        //when
        val partner = partnerClient.getPartnerBy(brn)

        //then
        then(partner.name).isEqualTo(name)
        then(partner.brn).isEqualTo(brn)
    }
}