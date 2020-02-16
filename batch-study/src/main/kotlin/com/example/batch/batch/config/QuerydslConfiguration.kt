package com.example.batch.batch.config

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.persistence.EntityManager

@Configuration
class QuerydslConfiguration(
    private val entityManager: EntityManager
) {

    @Bean
    fun jpaQueryFactory(): JPAQueryFactory {
        return JPAQueryFactory(entityManager)
    }
}