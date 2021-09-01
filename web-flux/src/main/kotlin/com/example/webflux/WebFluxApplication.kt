package com.example.webflux

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.stereotype.Component

@SpringBootApplication
class WebFluxApplication

fun main(args: Array<String>) {
    runApplication<WebFluxApplication>(*args)
}

@Component
class RepositoryDatabaseLoader {

//    @Bean
//    fun initialize(
//        blockingItemRepository: BlockingItemRepository
//    ) = CommandLineRunner {
//        blockingItemRepository.save(Item("Alf alarm clock", 19.99))
//        blockingItemRepository.save(Item("Smurf Tv tray", 24.99))
//    }

    @Bean
    fun initialize(
//        blockingItemRepository: BlockingItemRepository,
        mongo: MongoOperations
    ) = CommandLineRunner {
        mongo.save(Item("Alf alarm clock", 19.99))
        mongo.save(Item("Smurf Tv tray", 24.99))
    }
}