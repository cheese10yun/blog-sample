package com.example.kafkastudy

import org.springframework.boot.fromApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.with
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestKafkaStudyApplication {

    @Bean
    @ServiceConnection
    fun kafkaContainer(): KafkaContainer {
        return KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
    }

    @Bean
    @ServiceConnection
    fun mongoDbContainer(): MongoDBContainer {
        return MongoDBContainer(DockerImageName.parse("mongo:latest"))
    }

    @Bean
    @ServiceConnection
    fun mysqlContainer(): MySQLContainer<*> {
        return MySQLContainer(DockerImageName.parse("mysql:latest"))
    }

}

fun main(args: Array<String>) {
    fromApplication<KafkaStudyApplication>().with(TestKafkaStudyApplication::class).run(*args)
}
