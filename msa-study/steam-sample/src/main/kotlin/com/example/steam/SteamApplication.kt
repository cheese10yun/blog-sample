package com.example.steam

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Source

@SpringBootApplication
@EnableBinding(Source::class)
class SteamApplication

fun main(args: Array<String>) {
    runApplication<SteamApplication>(*args)
}
