package com.server.gateway

//import org.springframework.cloud.netflix.eureka.EnableEurekaClient

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
//@EnableDiscoveryClient
//@EnableEurekaClient
class GatewayServerApplication

fun main(args: Array<String>) {
    System.setProperty("reactor.netty.http.server.accessLogEnabled", "true")
    runApplication<GatewayServerApplication>(*args)
}