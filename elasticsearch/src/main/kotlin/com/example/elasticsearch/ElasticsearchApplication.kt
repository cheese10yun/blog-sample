package com.example.elasticsearch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ElasticsearchApplication

fun main(args: Array<String>) {
    runApplication<ElasticsearchApplication>(*args)
}
