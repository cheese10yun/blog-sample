package com.spring.camp.io

import org.mockito.Mockito.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary


@TestConfiguration
class ClientTestConfiguration {

    @Bean
    @Primary
    fun partnerClient() = mock(PartnerClient::class.java)
}