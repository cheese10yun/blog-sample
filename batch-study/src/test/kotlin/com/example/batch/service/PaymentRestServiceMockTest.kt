package com.example.batch.service


import com.example.batch.SpringBootTestSupport
import com.example.batch.domain.order.domain.Payment
import com.example.batch.domain.order.dto.PaymentDto
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.math.BigDecimal
import java.net.URI
import kotlin.properties.Delegates.notNull


internal class PaymentRestServiceMockTest(
    private val paymentRestService: PaymentRestService,
    private val paymentRestTemplate: RestTemplate
) : SpringBootTestSupport() {

    private var mockServer: MockRestServiceServer by notNull()

    @BeforeEach
    internal fun setUp() {
        mockServer = MockRestServiceServer.createServer(paymentRestTemplate)
    }

    @Test
    internal fun `requestPayment mock test`() {
        //given
        val amount = BigDecimal(100)
        val pageSize = 1
        val size = 10
        val path = "/json/payment-page.json"

        val url = UriComponentsBuilder.fromUri(URI.create("http://localhost:8080/payment"))
            .queryParam("amount", amount)
            .queryParam("page", pageSize)
            .queryParam("size", size)
            .build()

        mockServer
            .expect(requestTo(url.toUriString()))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ClassPathResource(path, javaClass))
            )


        //when
        val page = paymentRestService.requestPage<PaymentDto>(amount, pageSize, size)

        //then
        then(page.content).hasSize(10)
        then(page.totalPages).isEqualTo(224)
        then(page.totalElements).isEqualTo(2232)
        then(page.last).isFalse()
        then(page.first).isTrue()
        then(page.size).isEqualTo(10)
        then(page.number).isEqualTo(0)
        then(page.empty).isFalse()
    }
}