package com.spring.camp.io

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class ClientConfiguration {

    @Bean
    fun partnerClient(
    ) = PartnerClient(
        restTemplate = RestTemplateBuilder()
            .rootUri("http://localhost:8080")
            .build()
    )
}