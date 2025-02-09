package com.spring.camp.io

import java.math.BigDecimal
import java.time.LocalDate
import org.mockito.Mockito.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class ClientTestConfiguration {

    @Bean
    @Primary
    fun mockEmailClient() = mock(EmailClient::class.java)!!

    @Bean
    @Primary
    fun mockPartnerClient() = mock(PartnerClient::class.java)!!

    @Bean
    @Primary
    fun mockExchangeRateClient() = mock(ExchangeRateClient::class.java)!!

//    @Bean
//    @Primary
//    fun mockPartnerClientService() = mock(PartnerClientService::class.java)!!
}

class ExchangeRateClient {


    /**
     * 환율 정보를 조회하여 환율 값을 BigDecimal로 반환합니다.
     * @param fromCurrency 환율 조회 기준 통화 (예: "USD")
     * @param toCurrency 환율 조회 대상 통화 (예: "KRW")
     * @param baseDate 기준일 (예: LocalDate.of(2024, 1, 1))
     * @return 환율 값 (BigDecimal)
     */
    fun getExchangeRate(fromCurrency: String, toCurrency: String, baseDate: LocalDate): BigDecimal {

        return 1_457.80.toBigDecimal()
    }
}

class EmailClient

