package com.example.boot3mongo

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

fun <A : Any> A.logger(): Lazy<Logger> = lazy { LoggerFactory.getLogger(this.javaClass) }

@SpringBootApplication
class Boot3MongoApplication

fun main(args: Array<String>) {
    runApplication<Boot3MongoApplication>(*args)
}
