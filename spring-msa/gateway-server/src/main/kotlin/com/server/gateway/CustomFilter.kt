package com.server.gateway

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.cloud.sleuth.Tracer
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
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


@Component
@Order(-1)
class GlobalExceptionHandler(
    private val objectMapper: ObjectMapper
) : ErrorWebExceptionHandler {
    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val response = exchange.response
        var statusCode = 0;

        if (response.isCommitted) {
            return Mono.error(ex)
        }

        response.headers.contentType = MediaType.APPLICATION_JSON
        if (ex is ResponseStatusException) {
            response.statusCode = ex.status
            statusCode = ex.rawStatusCode
        }

        return response.writeWith(Mono.fromSupplier {
            val bufferFactory = response.bufferFactory()
            try {

                val errorResponse = objectMapper.writeValueAsBytes(
                    ErrorResponse(
                        message = ex.message ?: "error message",
                        status = statusCode,
                        errors = listOf(),
                        code = "C001",
                    )
                )
                return@fromSupplier bufferFactory.wrap(errorResponse)
            } catch (e: Exception) {
                return@fromSupplier bufferFactory.wrap(ByteArray(0))
            }
        })
    }
}

class ErrorResponse(
    val message: String,
    val status: Int,
    val errors: List<FieldError>,
    val code: String,
)

class FieldError(
    val field: String,
    val value: String,
    val reason: String
)
