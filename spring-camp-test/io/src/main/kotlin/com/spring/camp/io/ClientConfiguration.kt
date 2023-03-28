package com.spring.camp.io

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ClientConfiguration {

    @Bean
    fun partnerClient() = PartnerClient(
        restTemplate = RestTemplateBuilder()
            .rootUri("http://localhost:8080")
            .build()
    )
}