package com.spring.camp.io

import org.springframework.stereotype.Service

@Service
class PartnerClientService(
    private val partnerClient: PartnerClient
) {

    /**
     * 2xx 응답이 아닌 경우 Business Logic에 맞게 설정
     */
    fun getPartnerBy(brn: String): PartnerResponse {
        val response = partnerClient.getPartnerByResponse(brn)
        if (response.statusCode.is2xxSuccessful.not()){
            throw IllegalArgumentException("....")
        }
        return response.body!!
    }
}