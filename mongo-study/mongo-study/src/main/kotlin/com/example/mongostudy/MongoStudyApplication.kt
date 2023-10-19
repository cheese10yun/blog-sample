package com.example.mongostudy

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.MongoRepository

fun <A : Any> A.logger(): Lazy<Logger> = lazy { LoggerFactory.getLogger(this.javaClass) }

@SpringBootApplication
class MongoStudyApplication

fun main(args: Array<String>) {
    runApplication<MongoStudyApplication>(*args)
}



