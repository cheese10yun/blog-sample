package com.server.gateway

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
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
            is ResponseStatusException -> ErrorResponse(code = ErrorCode.FRAME_WORK_INTERNAL_ERROR)
            is BusinessException -> {
                response.statusCode = HttpStatus.valueOf(ex.errorCode.status)
                ErrorResponse(code = ex.errorCode)
            }
            // 그외 오류는 ErrorCode.UNDEFINED_ERROR 기반으로 메시지를 사용
            else -> {
                response.statusCode = HttpStatus.valueOf(ErrorCode.UNDEFINED_ERROR.status)
                ErrorResponse(code = ErrorCode.UNDEFINED_ERROR)
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