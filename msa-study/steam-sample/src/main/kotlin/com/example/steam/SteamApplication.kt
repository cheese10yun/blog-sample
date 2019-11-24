package com.example.steam

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SteamApplication

fun main(args: Array<String>) {
    runApplication<SteamApplication>(*args)
}
