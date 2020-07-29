package com.example.configclient

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ConfigClientApplication

fun main(args: Array<String>) {
    runApplication<ConfigClientApplication>(*args)
}
