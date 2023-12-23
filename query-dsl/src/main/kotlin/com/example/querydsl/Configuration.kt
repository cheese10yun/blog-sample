package com.example.querydsl

import com.fasterxml.jackson.databind.ObjectMapper
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.time.Duration
import jakarta.persistence.EntityManager

@Configuration
class Configuration {

    @Bean
    fun query(entityManager: EntityManager): JPAQueryFactory {
        return JPAQueryFactory(entityManager)
    }

    @Bean
    fun restTemplate(objectMapper: ObjectMapper): RestTemplate {
        return RestTemplateBuilder()
            .rootUri("http://localhost:8080")
            .setConnectTimeout(Duration.ofSeconds(10))
            .additionalMessageConverters()
            .build()
    }
}