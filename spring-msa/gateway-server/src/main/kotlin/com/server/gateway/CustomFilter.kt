package com.server.gateway

import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.cloud.sleuth.Tracer
import org.springframework.stereotype.Component

@Component
class GlobalFilter : AbstractGatewayFilterFactory<GlobalFilter.Config>(Config::class.java) {

    private val log by logger()

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            // 인증 관련 로직이 있다고 가정 하고, 인증이 실패하는 경우 라고 가정
            check(config.preLogger) { "check 메서드..." }

            chain.filter(exchange)
        }
    }

    class Config(
        val preLogger: Boolean,
        val postLogger: Boolean
    )
}