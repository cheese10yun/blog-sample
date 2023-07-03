package com.spring.camp.io

import org.springframework.http.ResponseEntity
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

    /**
     * 2xx 응답이 아닌 경우 호출하는 곳에서 제어하게 변경
     */
    fun getPartner(brn: String): ResponseEntity<PartnerResponse> {
        return partnerClient.getPartnerByResponse(brn)
    }

//    /**
//     * Pair<Int, PartnerResponse?> 리턴
//     */
//    fun getPartner(brn: String): Pair<Int, PartnerResponse?> {
//        val partnerByResponse = partnerClient.getPartnerByResponse(brn)
//        return Pair(
//            first = partnerByResponse.statusCode.value(),
//            second = partnerByResponse.body
//        )
//    }
}