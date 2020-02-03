package com.example.querydsl.service

import com.example.querydsl.api.PaymentApi
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val bankClient: BankClient
) {


    fun doPayment(dto: PaymentApi.BankAccountPayment) {
        val isMatched = bankClient.requestAccount(dto.bankAccount, dto.bankAccount, dto.bankCode)
        if (isMatched.not()) {
            throw RuntimeException("계좌번호 계좌주명 불일치")
        }
        // 무통장 결제 진행...
    }


}