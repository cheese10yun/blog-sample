package com.spring.camp.api

import com.spring.camp.io.PartnerClient
import com.spring.camp.io.PartnerClientService
import com.spring.camp.io.PartnerStatus
import com.spring.camp.io.PartnerStatusResponse
import org.springframework.stereotype.Service

@Service
class ShopRegistrationService(
    private val shopRepository: ShopRepository,
    private val partnerClientService: PartnerClientService,
) {

//    fun register(
//        brn: String,
//    ): Shop {
//        val response = partnerClient.getPartnerByResponse(brn)
//        // 2xx가 아닌 경우는 예외 발생
//        require(!response.statusCode.is2xxSuccessful.not()) { "error message ..." }
//        return shopRepository.save(
//            Shop(
//                brn = brn,
//                name = response.body!!.name
//            )
//        )
//    }

    fun register(
        brn: String,
    ): Shop {
        val partnerResponse = partnerClientService.getPartner(brn)
        val shop = when {
            partnerResponse.statusCode.is2xxSuccessful.not() -> {
                // 조회 실패시 추가 질의 로직...
                Shop(
                    brn = brn,
                    name = "국세청에서 응답받은 가맹점명"
                )
            }
            else -> Shop(
                brn = brn,
                name = partnerResponse.body!!.name
            )
        }
        return shopRepository.save(shop)
    }
}

@Service
class ShopLatestUpdateService(
    private val partnerClient: PartnerClient,
) {

    fun sync(brn: String) {
        val partner = partnerClient.getPartnerStatus(brn)
        when (partner.status) {
            PartnerStatus.OUT_OF_BUSINESS -> {
                // 폐업에 따른 추가 액션
            }

            PartnerStatus.OPEN -> {
                // 정상에 띠른 추가 액션
            }

            PartnerStatus.CLOSING -> {
                // 휴업에 띠른 추가 액션
            }
            // .. 기타등등에 따른 추가 액션
        }
    }
}