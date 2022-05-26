package com.example.msaerrorresponse

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.interceptors.LogRequestInterceptor
import com.github.kittinunf.fuel.core.interceptors.LogResponseInterceptor
import org.springframework.cloud.sleuth.Tracer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FuelConfiguration {
    @Bean
    fun fuelManager(tracer: Tracer) =
        FuelManager.instance.apply {
            this.timeoutReadInMillisecond = 120_000 // 2분
            this.timeoutReadInMillisecond = 120_000 // 2분
            this.addRequestInterceptor(tracingRequestInterceptor(tracer = tracer))
            this.addRequestInterceptor(LogRequestInterceptor)
            this.addResponseInterceptor(LogResponseInterceptor)

        }

    private fun tracingRequestInterceptor(tracer: Tracer) = { next: (Request) -> Request ->
        { request: Request ->
            val span = tracer.currentSpan() ?: tracer.nextSpan()
            request.header(
                "x-b3-traceid" to span.context().traceId(),
                "x-b3-spanid" to tracer.nextSpan().context().spanId(),
                "x-b3-parentspanid" to tracer.nextSpan().context().parentId().toString()
            )
            next(request)
        }
    }
}

// a6ee138d037dfaea = a1
// a6ee138d037dfaea = b1
// a6ee138d037dfaea = c1