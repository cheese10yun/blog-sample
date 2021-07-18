package com.server.gateway

//import org.springframework.cloud.netflix.eureka.EnableEurekaClient

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SpringBootApplication
//@EnableDiscoveryClient
//@EnableEurekaClient
class GatewayServerApplication

fun main(args: Array<String>) {
    System.setProperty("reactor.netty.http.server.accessLogEnabled", "true")
    runApplication<GatewayServerApplication>(*args)
}

@Configuration
class Configuration {

    @Bean
    fun httpTraceRepository() =
        InMemoryHttpTraceRepository()

}

fun <A : Any> A.logger(): Lazy<Logger> = lazy { LoggerFactory.getLogger(this.javaClass) }