package com.example.batch.service

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.math.BigDecimal
import java.net.URI

@Service
class RestService(
    private val restTemplate: RestTemplate
) {


    fun requestPayment(amount: BigDecimal, page: Int, size: Int) {



        val forEntity = restTemplate.getForEntity(UriComponentsBuilder.fromUri(URI.create("/payment"))
            .queryParam("amount", amount)
            .queryParam("page", page)
            .queryParam("size", size)
            .build()
            .toUriString(), String::class.java)

    }


}