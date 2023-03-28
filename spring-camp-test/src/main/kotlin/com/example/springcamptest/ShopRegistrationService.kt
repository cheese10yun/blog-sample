package com.example.springcamptest

import org.springframework.stereotype.Service

@Service
class ShopRegistrationService(
    private val shopRepository: ShopRepository,
    private val partnerClient: PartnerClient,
) {

    fun register(
        brn: String,
    ): Shop {
        val partner = partnerClient.getPartnerBy(brn)
        return shopRepository.save(
            Shop(
                brn = brn,
                name = partner.name
            )
        )
    }
}