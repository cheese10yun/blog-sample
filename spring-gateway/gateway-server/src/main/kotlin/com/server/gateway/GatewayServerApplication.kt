package com.server.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.security.Principal


@SpringBootApplication
@EnableDiscoveryClient
@EnableEurekaClient
class GatewayServerApplication

fun main(args: Array<String>) {
    System.setProperty("reactor.netty.http.server.accessLogEnabled", "true")
    HttpStatus.Series.CLIENT_ERROR
    runApplication<GatewayServerApplication>(*args)
}

@Component
class SampleD{

    @Bean
    fun customGlobalFilter(): GlobalFilter? {
        return GlobalFilter { exchange: ServerWebExchange, chain: GatewayFilterChain ->
            exchange.getPrincipal<Principal>()
                .map<Any>(Principal::getName)
                .defaultIfEmpty("Default User")
                .map { userName: Any? ->
                    //adds header to proxied request
                    exchange.request.mutate().header("CUSTOM-REQUEST-HEADER", "userName").build()
                    exchange
                }
                .flatMap { exchange: ServerWebExchange? -> chain.filter(exchange) }
        }
    }

    @Bean
    fun customGlobalPostFilter(): GlobalFilter? {
        return GlobalFilter { exchange: ServerWebExchange, chain: GatewayFilterChain ->
            chain.filter(exchange)
                .then(Mono.just(exchange))
                .map { serverWebExchange: ServerWebExchange ->
                    //adds header to response
                    serverWebExchange.response.headers["CUSTOM-RESPONSE-HEADER"] = if (HttpStatus.OK == serverWebExchange.response.statusCode) "It worked" else "It did not work"
                    serverWebExchange
                }
                .then()
        }
    }
}