package com.example.msaerrorresponse

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.interceptors.LogRequestInterceptor
import com.github.kittinunf.fuel.core.interceptors.LogResponseInterceptor
import com.github.kittinunf.fuel.core.interceptors.cUrlLoggingRequestInterceptor
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.sleuth.Tracer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableAsync
class MsaErrorResponseApplication

fun main(args: Array<String>) {
    runApplication<MsaErrorResponseApplication>(*args)
}

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

@Component
class Runner(
    private val bookReservationRepository: BookReservationRepository
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        val toList = (1..20).map {
            BookReservation(
                bookId = it.toLong(),
                bookStatus = "OPEN",
                userId = it.toLong(),
            )
        }
            .toList()
        bookReservationRepository.saveAll(toList)
    }
}