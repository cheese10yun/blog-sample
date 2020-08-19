package com.server.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GatewayServer

fun main(args: Array<String>) {
    runApplication<GatewayServer>(*args)
}