package com.example.msaerrorresponse

import org.slf4j.LoggerFactory
import org.springframework.cloud.sleuth.Tracer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/b-service")
class BServiceApi {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun b(){
    }
}