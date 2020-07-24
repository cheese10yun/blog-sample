package com.example.springdocker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class SpringDockerApplication

fun main(args: Array<String>) {
    runApplication<SpringDockerApplication>(*args)
}

@RestController
class GreetingsController {

    @GetMapping("/greetings")
    fun greetings() = "hello form docker"
}