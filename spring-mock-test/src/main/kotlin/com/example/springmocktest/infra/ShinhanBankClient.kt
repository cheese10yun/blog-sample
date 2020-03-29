package com.example.springmocktest.infra

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ShinhanBankClient(
    private val restTemplate: RestTemplate
) {

    fun verifyAccountHolder(accountNumber: String, accountHolder: String) {

        val response = ShinChanBankApi().checkAccountHolder(accountHolder, accountNumber)
        require(!response.matched.not()) { "계좌주명이 일치하지 않습니다." }
    }

}

class ShinChanBankApi {

    fun checkAccountHolder(accountHolder: String, accountNumber: String): AccountHolderVerificationResponse {
        return when {
            accountHolder == "yun" && accountNumber == "110-2222-2222" -> AccountHolderVerificationResponse(true)
            else -> AccountHolderVerificationResponse(false)
        }
    }
}

data class AccountHolderVerificationResponse(
    val matched: Boolean
)