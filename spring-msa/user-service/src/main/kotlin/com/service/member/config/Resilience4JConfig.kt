package com.service.member.config

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import java.time.Duration
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder
import org.springframework.cloud.client.circuitbreaker.Customizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

//@Configuration
//class Resilience4JConfig {
//
//    @Bean
//    fun globalCustomConfiguration(): Customizer<Resilience4JCircuitBreakerFactory> {
//
//        val circuitBreakerConfig = CircuitBreakerConfig.custom()
//            .failureRateThreshold(4f) // CircuitBeaker를 열지 결정하는 failure rate threshold percentage, default: 50
//            .waitDurationInOpenState(Duration.ofSeconds(5)) // CircuitBeaker를 Open한 상태를 유지하는 지속 시간, 이 기간 이후 half-open 상태, default 60s
//            .slidingWindowType(SlidingWindowType.COUNT_BASED) // CircuitBeaker가 닫힐 때 통화 결과를 기록하는데 사용되는 슬라이딩 유형을 구성, 카운트 기반 or 시간 기반으로 작성
//            .slidingWindowSize(2) // CircuitBeaker가 닫힐 때 호출 결과를 기록하는데 사용되는 슬라이딩 창의 크기를 구성, default: 100
//            .build()
//
//        val timeLimiterConfig =
//            TimeLimiterConfig.custom() // future supplier의 time limit을 정하는 API, default: 1초
//                .timeoutDuration(Duration.ofSeconds(4))
//                .build()
//
//        return Customizer { factory ->
//            factory.configureDefault { id ->
//                Resilience4JConfigBuilder(id)
//                    .timeLimiterConfig(timeLimiterConfig)
//                    .circuitBreakerConfig(circuitBreakerConfig)
//                    .build()
//            }
//        }
//    }
//}