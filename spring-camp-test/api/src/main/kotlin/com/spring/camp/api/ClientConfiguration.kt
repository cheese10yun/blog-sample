package com.spring.camp.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class ClientConfiguration {

    @Bean("bankAccountClient")
    @Profile("production", "sandbox")
    fun bankAccountClient(): BankAccountClient {
        return BankAccountClientImpl()
    }

    @Bean("bankAccountClient")
    @Profile("beta", "test")
    fun mockBankAccountClient(): BankAccountClient {
        return BankAccountClientMock()
    }
}