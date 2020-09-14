package com.server.gateway.error

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

class MyWebExceptionHandler : ErrorWebExceptionHandler {
    private fun errorCodeMaker(errorCode: Int): String {
        return "{\"errorCode\":$errorCode}"
    }

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
//        logger.warn("in GATEWAY Exeptionhandler : " + ex);
        var errorCode = 999
        if (ex.javaClass == NullPointerException::class.java) {
            errorCode = 61
        }
        val bytes = errorCodeMaker(errorCode).toByteArray(StandardCharsets.UTF_8)
        val buffer = exchange.response.bufferFactory().wrap(bytes)
        return exchange.response.writeWith(Flux.just(buffer))
    }
}