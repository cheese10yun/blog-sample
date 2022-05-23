package com.example.msaerrorresponse

import com.github.kittinunf.fuel.core.Headers.Companion.CONTENT_TYPE
import com.github.kittinunf.fuel.httpGet
import org.slf4j.LoggerFactory
import org.springframework.cloud.sleuth.Tracer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/a-service")
class AServiceApi {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun a() {

        val header = "http://localhost:8686/b-service"
            .httpGet()
            .header(CONTENT_TYPE to "application/json")
            .response()


    }
}
