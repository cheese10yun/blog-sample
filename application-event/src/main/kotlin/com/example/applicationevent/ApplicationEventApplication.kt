package com.example.applicationevent

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class ApplicationEventApplication

fun main(args: Array<String>) {
    runApplication<ApplicationEventApplication>(*args)
}
