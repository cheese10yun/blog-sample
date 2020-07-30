package com.example.configclient

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.context.config.annotation.RefreshScope

@SpringBootApplication
class ConfigClientApplication

fun main(args: Array<String>) {
    runApplication<ConfigClientApplication>(*args)
}
