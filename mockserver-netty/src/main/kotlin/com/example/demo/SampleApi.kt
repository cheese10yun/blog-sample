package com.example.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sample")
class SampleApi {

    @GetMapping
    fun getSample() = Sample("foo", "bar")

    data class Sample(
        val foo: String,
        val bar: String
    )
}