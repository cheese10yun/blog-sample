package com.example.kafkastudy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaStudyApplication

fun main(args: Array<String>) {
    runApplication<KafkaStudyApplication>(*args)
}
