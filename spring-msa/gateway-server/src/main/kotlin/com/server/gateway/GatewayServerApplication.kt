package com.server.gateway

//import org.springframework.cloud.netflix.eureka.EnableEurekaClient

import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

//@Component
//class CustomFilter : AbstractGatewayFilterFactory<CustomFilter.Config>() {
//
//    class Config {
//
//    }
//
//    override fun apply(config: Config?): GatewayFilter {
//
//        return
//    }
//}

fun <A : Any> A.logger(): Lazy<Logger> = lazy { LoggerFactory.getLogger(this.javaClass) }