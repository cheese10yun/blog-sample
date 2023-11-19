package com.example.restdocssample

import org.junit.jupiter.api.Test

class MemberKtorClientTest {

    private val memberClient: MemberKtorClient = MemberKtorClient()

    @Test
    fun xxx() {
        val member = memberClient.getMember(1L)

        member
            .onFailure { errorResponse: ErrorResponse ->
                // 오류 발생시 넘겨 받은 errorResponse 객체로 추가 핸들링 가능
            }
    }
}