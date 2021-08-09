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

@Configuration
class Resilience4JConfig {

    @Bean
    fun globalCustomConfiguration(): Customizer<Resilience4JCircuitBreakerFactory> {
        val circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(4f)
            .waitDurationInOpenState(Duration.ofMillis(1000))
            .slidingWindowType(SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(2)
            .build()

        val timeLimiterConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofMillis(4000))
            .build()

        return Customizer { factory: Resilience4JCircuitBreakerFactory ->
            factory.configureDefault { id: String ->
                Resilience4JConfigBuilder(id)
                    .circuitBreakerConfig(circuitBreakerConfig)
                    .timeLimiterConfig(timeLimiterConfig)
                    .build()
            }
        }
    }
}