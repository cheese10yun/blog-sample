package com.example.kafkasample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaSampleApplication

fun main(args: Array<String>) {
    runApplication<KafkaSampleApplication>(*args)
}
