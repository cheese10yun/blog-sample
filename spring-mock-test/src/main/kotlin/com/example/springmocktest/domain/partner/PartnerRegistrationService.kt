package com.example.springmocktest.domain.partner

import com.example.springmocktest.api.PartnerRegistrationRequest
import com.example.springmocktest.infra.ShinhanBankClient
import org.springframework.stereotype.Service

@Service
class PartnerRegistrationService(
    private val partnerRepository: PartnerRepository,
    private val bankClient: ShinhanBankClient
) {

    fun register(dto: PartnerRegistrationRequest): Partner {


        bankClient.verifyAccountHolder(
            accountHolder = dto.accountHolder,
            accountNumber = dto.accountHolder
        )


        return partnerRepository.save(Partner(
            accountNumber = dto.accountNumber,
            accountHolder = dto.accountHolder,
            name = dto.name
        ))


    }
}