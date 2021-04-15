package com.server.gateway

import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class CustomFilter : AbstractGatewayFilterFactory<FilterConfig>(FilterConfig::class.java) {
    val log by logger()

    override fun apply(config: FilterConfig): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request
            val response = exchange.response
            log.info("CustomFilter request id: ${request.id}")
            chain.filter(exchange).then(Mono.fromRunnable { log.info("CustomFilter response status code: ${response.statusCode}") })
        }
    }

}

@Component
class GlobalFilter : AbstractGatewayFilterFactory<FilterConfig>(FilterConfig::class.java) {
    val log by logger()

    override fun apply(config: FilterConfig): GatewayFilter {

        return GatewayFilter { exchange, chain ->
            val request = exchange.request
            val response = exchange.response
            log.info("Global request id: ${request.id}")
            chain.filter(exchange).then(Mono.fromRunnable { log.info("Global response status code: ${response.statusCode}") })
        }
    }
}

data class FilterConfig(
        val message: String,
        val preLogger: Boolean,
        val postLogger: Boolean
)