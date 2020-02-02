package com.example.batch.service

import com.example.batch.common.PageResponse
import com.example.batch.domain.order.domain.Payment
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.math.BigDecimal
import java.net.URI

@Service
class PaymentRestService(
    private val paymentRestTemplate: RestTemplate
) {


    fun requestPayment(amount: BigDecimal, page: Int, size: Int): PageResponse<Payment> {
        val url = UriComponentsBuilder.fromUri(URI.create("http://localhost:8080/payment"))
            .queryParam("amount", amount)
            .queryParam("page", page)
            .queryParam("size", size)
            .build()

        val request = RequestEntity<Any>(HttpMethod.GET, url.toUri())
        val respType = object: ParameterizedTypeReference<PageResponse<Payment>>(){}

        return paymentRestTemplate.exchange(request, respType).body!!
    }
}


