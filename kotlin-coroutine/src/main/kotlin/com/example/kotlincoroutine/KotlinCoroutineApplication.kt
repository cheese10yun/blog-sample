package com.example.kotlincoroutine

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KotlinCoroutineApplication

fun main(args: Array<String>) {
    runApplication<KotlinCoroutineApplication>(*args)
}


fun <A : Any> A.logger2(): Lazy<Logger> = lazy { LoggerFactory.getLogger(this.javaClass) }