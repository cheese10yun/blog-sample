package com.example.demo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.jackson.responseObject
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDate

@Configuration
class AppConfiguration {
    @Bean
    @Profile("production | sandbox") // 특정 환경에서만 등록 하는 경우
//    @Profile("!test") // 특정 환경만 제외하는 경우
    fun exchangeRateClient() = ExchangeRateClientImpl()
}

interface ExchangeRateClient {
    fun getExchangeRate(
        targetDate: LocalDate,
        currencyForm: String,
        currencyTo: String,
    ): ExchangeRateResponse
}

@Service("exchangeRateClient")
class ExchangeRateClientImpl : ExchangeRateClient {

    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .apply { this.propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE }

    override fun getExchangeRate(
        targetDate: LocalDate,
        currencyForm: String,
        currencyTo: String,
    ): ExchangeRateResponse {
        val response = "http://localhost:8080/exchange-rate"
            .httpGet(
                parameters = listOf(
                    "targetDate" to targetDate,
                    "currencyForm" to currencyForm,
                    "currencyTo" to currencyTo
                )
            )
            .response()

        if (response.second.statusCode / 100 != 2) {
            // HTTP Status Code 2xx 아닌 경우는 어떻게
            throw IllegalStateException("HTTP Status Code: ${response.second.statusCode} ")
        }

        return response
            .first.responseObject<ExchangeRateResponse>(objectMapper)
            .third.get()
    }
}