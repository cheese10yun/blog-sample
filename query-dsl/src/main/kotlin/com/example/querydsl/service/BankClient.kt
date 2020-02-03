package com.example.querydsl.service

import org.springframework.stereotype.Service

@Service
class BankClient(

) {

    fun requestAccount(accountNumber: String, accountHolder: String, bankCode: String): Boolean {
        // 외부 모듈으로 계좌주명, 계좌 번호 기반으로 일치 여부를 확인
        return true
    }
}