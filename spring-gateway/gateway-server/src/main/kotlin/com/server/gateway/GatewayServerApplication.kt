package com.server.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@SpringBootApplication
@EnableDiscoveryClient
@EnableEurekaClient
class GatewayServerApplication

fun main(args: Array<String>) {
    System.setProperty("reactor.netty.http.server.accessLogEnabled", "true")
    runApplication<GatewayServerApplication>(*args)
}