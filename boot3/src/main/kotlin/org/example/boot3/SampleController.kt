package org.example.boot3

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/sample")
class SampleController {

    @GetMapping("/hello")
    fun hello(): SampleResponse {

        return SampleResponse(
            name = "Hello, World!"
        )
    }
}

data class SampleResponse(
    val name: String
)