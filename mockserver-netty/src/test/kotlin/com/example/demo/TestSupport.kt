package com.example.demo

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import java.time.LocalDate

@TestConfiguration
class TestSupport {

    @Bean
    @Profile("test")
    fun exchangeRateClient() = ExchangeRateClientMock()
}


class ExchangeRateClientMock : ExchangeRateClient {

    override fun getExchangeRate(targetDate: LocalDate, currencyForm: String, currencyTo: String): ExchangeRateResponse {
        return when (targetDate) {
            LocalDate.of(2022, 2, 2) -> ExchangeRateResponse(12000.12.toBigDecimal())
            LocalDate.of(2022, 2, 3) -> ExchangeRateResponse(13000.12.toBigDecimal())
            LocalDate.of(2022, 2, 4) -> ExchangeRateResponse(14000.12.toBigDecimal())
            else -> ExchangeRateResponse(15000.12.toBigDecimal())
        }
    }
}