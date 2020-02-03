package com.example.querydsl

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
@ActiveProfiles("test")
@Testcontainers
abstract class SpringBootTestSupport {

    companion object {
        @JvmStatic
        @Container
        val container = PostgreSQLContainer<Nothing>()
            .apply {
                withDatabaseName("querydsl")
            }
    }
}