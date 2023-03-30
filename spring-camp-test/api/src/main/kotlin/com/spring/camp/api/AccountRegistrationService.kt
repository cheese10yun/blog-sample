package com.spring.camp.api

import org.springframework.stereotype.Service

@Service
class AccountRegistrationService(
    private val bankAccountClient: BankAccountClient,
) {

    fun register(
        accountNumber: String,
        accountHolder: String,
        bankCode: String,
    ) {
        // isMatched 일치 하는 경우에만 저장
        val isMatched = bankAccountClient.isMatchedAccount(
            accountNumber = accountNumber,
            accountHolder = accountHolder,
            bankCode = bankCode
        )
        println(isMatched)
        // 계좌 저장 로직...
    }
}