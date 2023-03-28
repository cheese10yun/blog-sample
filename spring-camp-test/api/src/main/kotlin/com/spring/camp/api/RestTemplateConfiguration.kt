package com.spring.camp.api

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfiguration {

    @Bean
    fun localRestTemplate(): RestTemplate {
        return RestTemplateBuilder()
            .rootUri("http://localhost:8080")
            .build()
    }


}