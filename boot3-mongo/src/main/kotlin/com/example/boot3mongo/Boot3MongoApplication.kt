package com.example.boot3mongo

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableMongoAuditing

fun <A : Any> A.logger(): Lazy<Logger> = lazy { LoggerFactory.getLogger(this.javaClass) }

@SpringBootApplication
@EnableMongoAuditing
class Boot3MongoApplication

fun main(args: Array<String>) {
    runApplication<Boot3MongoApplication>(*args)
}
