package com.spring.camp.api

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class AccountRegistrationServiceTest(
    private val accountRegistrationService: AccountRegistrationService,
) : TestSupport() {

    @Test
    fun `register test`() {
        accountRegistrationService.register(
            accountNumber = "110-xxxx-xxxx",
            accountHolder = "홍길동",
            bankCode = "032"
        )
    }
}