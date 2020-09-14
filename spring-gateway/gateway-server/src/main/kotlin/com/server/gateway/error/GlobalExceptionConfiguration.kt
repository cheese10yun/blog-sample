package com.server.gateway.error

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Order(-1)
@Component
class GlobalExceptionConfiguration(private val objectMapper: ObjectMapper) : ErrorWebExceptionHandler {
    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val response = exchange.response
        if (response.isCommitted) {
            return Mono.error(ex)
        }

        // header set
        response.headers.contentType = MediaType.APPLICATION_JSON
        if (ex is ResponseStatusException) {
            response.statusCode = ex.status
        }
        return response
            .writeWith(Mono.fromSupplier {
                val bufferFactory = response.bufferFactory()
                try {
                    return@fromSupplier bufferFactory.wrap(objectMapper.writeValueAsBytes(ErrorR("123")))
                } catch (e: JsonProcessingException) {
//                    log.warn("Error writing response", ex);
                    return@fromSupplier bufferFactory.wrap(ByteArray(0))
                }
            })
    }

}

class ErrorR(
    val message: String
)