package com.example.mongostudy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.MongoRepository

@SpringBootApplication
class MongoStudyApplication

fun main(args: Array<String>) {
    runApplication<MongoStudyApplication>(*args)
}



