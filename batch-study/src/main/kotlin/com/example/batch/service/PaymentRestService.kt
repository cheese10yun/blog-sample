package com.example.batch.service

import com.example.batch.common.PageResponse
import com.example.batch.domain.order.domain.Payment
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
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
    private val paymentRestTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) :RestService{

    override fun <T> requestPage(amount: BigDecimal, page: Int, size: Int): PageResponse<T> {
        val url = UriComponentsBuilder.fromUri(URI.create("http://localhost:8080/payment"))
            .queryParam("amount", amount)
            .queryParam("page", page)
            .queryParam("size", size)
            .build()

        val body = paymentRestTemplate.getForObject(url.toUri(), String::class.java)!!
        return objectMapper.readValue<PageResponse<T>>(body, object : TypeReference<PageResponse<T>>() {})
    }

    fun requestPayment2(): List<Payment> {
        val url = UriComponentsBuilder.fromUri(URI.create("http://localhost:8080/payment/test"))
            .build()
        val request = RequestEntity<Any>(HttpMethod.GET, url.toUri())
        val respType = object : ParameterizedTypeReference<List<Payment>>() {}

        return paymentRestTemplate.exchange(request, respType).body!!
    }

}


