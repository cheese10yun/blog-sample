package com.spring.camp.io

import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.time.LocalDate

@AllOpen
class PartnerClient(
    private val restTemplate: RestTemplate,
) {

    fun getPartnerBy(brn: String): PartnerResponse {
        return restTemplate.getForObject("/api/v1/partner/${brn}", PartnerResponse::class.java)!!
    }

    fun getPartnerByResponse(brn: String): ResponseEntity<PartnerResponse> {
        restTemplate.getForEntity("/api/v1/partner/${brn}", PartnerResponse::class.java, mapOf("asd" to "ads"))
        return restTemplate.getForEntity("/api/v1/partner/${brn}", PartnerResponse::class.java)
    }

    fun getPartnerStatus(
        brn: String,
    ): PartnerStatusResponse {
        return restTemplate.getForObject("/api/v1/partner/${brn}", PartnerStatusResponse::class.java)!!
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