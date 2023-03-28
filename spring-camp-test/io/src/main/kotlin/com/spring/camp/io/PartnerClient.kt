package com.spring.camp.io

import org.springframework.web.client.RestTemplate

class PartnerClient(
    private val restTemplate: RestTemplate,
) {

    fun getPartnerBy(brn: String): PartnerResponse {
        return restTemplate.getForObject("/api/v1/partner/${brn}", PartnerResponse::class.java)!!
    }

    fun syncPartner() {

    }
}

class PartnerResponse(
    val brn: String,
    val name: String,
)