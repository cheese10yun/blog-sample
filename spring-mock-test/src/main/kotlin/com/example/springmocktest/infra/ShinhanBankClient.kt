package com.example.springmocktest.infra

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
class ShinhanBankClient(
    private val shinChanBankApi: ShinChanBankApi
) {

    // 계좌주명, 계좝번호가 일치하지 않으면 예외 발생
    fun verifyAccountHolder(accountNumber: String, accountHolder: String) {
        val response = shinChanBankApi.checkAccountHolder(accountHolder, accountNumber)
        require(!response.matched.not()) { "계좌주명이 일치하지 않습니다." }
    }
}


interface ShinChanBankApi {
    fun checkAccountHolder(accountHolder: String, accountNumber: String): AccountHolderVerificationResponse
}

@Service("shinChanBankApi")
@Profile("production")
class ShinChanBankApiImpl : ShinChanBankApi {
    // 계좌주명, 계좌번호가 하드 코딩된 값과 일치여불르 확인한다.
    override fun checkAccountHolder(accountHolder: String, accountNumber: String): AccountHolderVerificationResponse {
        return when {
            accountHolder == "yun" && accountNumber == "110-2222-2222" -> AccountHolderVerificationResponse(true)
            else -> AccountHolderVerificationResponse(false)
        }
    }
}

@Service("shinChanBankApi")
@Profile("sandbox", "beta", "local", "test")
private class ShinChanBankApMock : ShinChanBankApi {
    // 어떤 값이 들어 와도 일치 한다고 가정한다
    override fun checkAccountHolder(accountHolder: String, accountNumber: String): AccountHolderVerificationResponse {
        return AccountHolderVerificationResponse(true)
    }
}


data class AccountHolderVerificationResponse(
    val matched: Boolean
)