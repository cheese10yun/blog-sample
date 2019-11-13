package com.example.kotlinjunit5

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(SampleProperties::class)
class KotlinJunit5Application

fun main(args: Array<String>) {
    runApplication<KotlinJunit5Application>(*args)
}
