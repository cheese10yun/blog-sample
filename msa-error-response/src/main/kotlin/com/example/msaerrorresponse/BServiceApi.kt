package com.example.msaerrorresponse

import org.slf4j.LoggerFactory
import org.springframework.cloud.sleuth.Tracer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/b-service")
class BServiceApi(
    private val tracer: Tracer
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun b(){

        val currentSpan = tracer.currentSpan()
        val nextSpan = tracer.nextSpan()
        val span = currentSpan ?: nextSpan

        log.info("=======b-service======")
        log.error("current traceId: ${currentSpan?.context()?.traceId()}")
        log.error("current spanId: ${currentSpan?.context()?.spanId()}")
        log.error("current parentId: ${currentSpan?.context()?.parentId()}")
        log.error("current sampled: ${currentSpan?.context()?.sampled()}")

        log.error("next traceId: ${nextSpan.context().traceId()}")
        log.error("next spanId: ${nextSpan.context().spanId()}")
        log.error("next parentId: ${nextSpan.context().parentId()}")
        log.error("next sampled: ${nextSpan.context().sampled()}")
        log.info("=======b-service======")
    }
}