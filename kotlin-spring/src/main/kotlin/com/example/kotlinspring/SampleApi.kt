package com.example.kotlinspring

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleApi {

    @GetMapping("/sample")
    fun hello() = "Hello World"

}

