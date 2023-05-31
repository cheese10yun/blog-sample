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
){
    fun asd(): Unit {

        //@formatter:off
        //@formatter:on
        //@formatter:off
        //@formatter:on

    }
}
