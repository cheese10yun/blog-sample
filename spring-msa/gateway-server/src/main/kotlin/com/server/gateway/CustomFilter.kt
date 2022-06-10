package com.server.gateway

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.cloud.sleuth.Tracer
import org.springframework.core.ResolvableType
import org.springframework.core.annotation.Order
import org.springframework.core.codec.Hints
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonEncoder
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
@Order(-1)// 내부 bean 보다 우선 순위를 높여 해당 빈이 동작하게 설정
class GlobalExceptionHandler(
    private val objectMapper: ObjectMapper
) : ErrorWebExceptionHandler {

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val response = exchange.response
        response.headers.contentType = MediaType.APPLICATION_JSON

        val errorResponse = when (ex) {
            // Spring Web Server 관련 오류의 경우 Spring 오류 메시지를 사용
            is ResponseStatusException ->
                ErrorResponse(
                    message = ex.message,
                    status = ex.rawStatusCode,
                    code = "C001"
                )
            // 그외 오류는 ErrorCode.UNDEFINED_ERROR 기반으로 메시지를 사용
            else -> {
                response.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
                ErrorResponse(
                    message = ex.message ?: "asd",
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    code = "C002"
                )
            }
        }

        return response.writeWith(
            Jackson2JsonEncoder(objectMapper).encode(
                Mono.just(errorResponse),
                response.bufferFactory(),
                ResolvableType.forInstance(errorResponse),
                MediaType.APPLICATION_JSON,
                Hints.from(Hints.LOG_PREFIX_HINT, exchange.logPrefix)
            )
        )
    }
}

class ErrorResponse(
    val message: String,
    val status: Int,
    val errors: List<FieldError> = emptyList(),
    val code: String,
)

class FieldError(
    val field: String,
    val value: String,
    val reason: String
)