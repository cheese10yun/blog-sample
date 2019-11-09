package com.example.sample

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class Config {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}