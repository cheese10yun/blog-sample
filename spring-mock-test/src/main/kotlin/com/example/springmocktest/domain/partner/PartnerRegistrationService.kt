package com.example.springmocktest.domain.partner

import com.example.springmocktest.api.PartnerRegistrationRequest
import org.springframework.stereotype.Service

@Service
class PartnerRegistrationService(
    private val partnerRepository: PartnerRepository
) {

    fun register(dto: PartnerRegistrationRequest): Partner {


        //


        return partnerRepository.save(Partner(
            accountNumber = dto.accountNumber,
            accountHolder = dto.accountHolder,
            name = dto.name
        ))


    }
}