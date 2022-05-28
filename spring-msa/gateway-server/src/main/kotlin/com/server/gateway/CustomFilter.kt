package com.server.gateway

import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.cloud.sleuth.Tracer
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

//@Component
//class CustomFilter : AbstractGatewayFilterFactory<CustomFilter.Config>(Config::class.java) {
//    val log by logger()
//
//    override fun apply(config: Config): GatewayFilter {
//        return GatewayFilter { exchange, chain ->
//            val request = exchange.request
//            val response = exchange.response
//            log.info("CustomFilter request id: ${request.id}")
//            chain.filter(exchange).then(Mono.fromRunnable { log.info("CustomFilter response status code: ${response.statusCode}") })
//        }
//    }
//
//    class Config
//}

@Component
class GlobalFilter(
    private val tracer: Tracer
) : AbstractGatewayFilterFactory<GlobalFilter.Config>(Config::class.java) {
    val log by logger()

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request
            val response = exchange.response

            val currentSpan = tracer.currentSpan()
            val nextSpan = tracer.nextSpan()
            val span = currentSpan ?: nextSpan
            val context = span.context()

            log.info("=======test======")
            log.error("current traceId: ${currentSpan?.context()?.traceId()}")
            log.error("current spanId: ${currentSpan?.context()?.spanId()}")
            log.error("current parentId: ${currentSpan?.context()?.parentId()}")
            log.error("current sampled: ${currentSpan?.context()?.sampled()}")

            log.error("next traceId: ${nextSpan.context().traceId()}")
            log.error("next spanId: ${nextSpan.context().spanId()}")
            log.error("next parentId: ${nextSpan.context().parentId()}")
            log.error("next sampled: ${nextSpan.context().sampled()}")
            log.info("=======test======")

            log.info("GlobalFilter request id: ${request.id}")

            chain.filter(exchange)
        }
    }

    class Config
}
