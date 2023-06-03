package com.example.intellijtest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sample")
class SampleController {

    @GetMapping
    fun getSample(): Sample {
        return Sample("test")
    }
}

data class Sample(
    val name: String,
) {
    fun asd(): Unit {
        //@formatter:off
        //@formatter:on
        val a = 10
            val b = 10
            val c = 10
            val d = 10
        val e = 10
        val f = 10
    }
}
