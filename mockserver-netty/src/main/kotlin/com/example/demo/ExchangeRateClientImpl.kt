package com.example.demo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.jackson.responseObject
import org.springframework.stereotype.Service
import java.time.LocalDate

interface ExchangeRateClient {
    fun getExchangeRate(
        targetDate: LocalDate,
        currencyForm: String,
        currencyTo: String,
    ): ExchangeRateResponse
}

@Service
class ExchangeRateClientImpl(
    private val host: String = "http://localhost:8080",
    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .apply { this.propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE }
) : ExchangeRateClient {

    override fun getExchangeRate(
        targetDate: LocalDate,
        currencyForm: String,
        currencyTo: String,
    ) =

        "$host/exchange-rate"
            .httpGet(
                parameters = listOf(
                    "targetDate" to targetDate,
                    "currencyForm" to currencyForm,
                    "currencyTo" to currencyTo
                )
            )
            .response()
            .first.responseObject<ExchangeRateResponse>(objectMapper)
            .third.get()


}