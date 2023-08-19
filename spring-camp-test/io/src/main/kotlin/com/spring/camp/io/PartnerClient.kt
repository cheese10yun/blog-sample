package com.spring.camp.io

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.time.LocalDate


@AllOpen
class PartnerClient(
    private val partnerClientRestTemplate: RestTemplate,
) {

//    fun getPartnerBy(brn: String): PartnerResponse {
//        return restTemplate
//            .getForObject(
//                "/api/v1/partner/${brn}",
//                PartnerResponse::class.java
//            )!!
//    }

    fun getPartnerBy(brn: String): PartnerResponse {
        val response = partnerClientRestTemplate.getForEntity("/api/v1/partner/${brn}", PartnerResponse::class.java)
//        if (response.statusCode.is2xxSuccessful.not()){
//            throw IllegalArgumentException("....")
//        }
        return response.body!!
    }

    fun getPartnerEntityBy(brn: String): ResponseEntity<PartnerResponse?> {
        return partnerClientRestTemplate.getForEntity("/api/v1/partner/${brn}", PartnerResponse::class.java)
    }

    fun getPartnerStatus(
        brn: String,
    ): PartnerStatusResponse {
        return partnerClientRestTemplate.getForObject("/api/v1/partner/${brn}", PartnerStatusResponse::class.java)!!
    }

    fun getPartners(
        brn: Set<String>,
    ): List<PartnerStatusResponse> {
        return partnerClientRestTemplate.getForObject("/api/v1/partner/${brn}", object : ParameterizedTypeReference<List<PartnerStatusResponse>>() {})
    }
}

class PartnerResponse(
    val brn: String,
    val name: String,
)

class PartnerStatusResponse(
    val status: PartnerStatus,
    val closeBusinessDate: LocalDate?,
)

enum class PartnerStatus(desc: String) {
    OUT_OF_BUSINESS("폐업"),
    OPEN("정상"),
    CLOSING("휴업")
    // 등등 상태 존재
}