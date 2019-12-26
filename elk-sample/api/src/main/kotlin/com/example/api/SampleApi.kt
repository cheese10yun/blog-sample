package com.example.api

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sample")
class SampleApi {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    fun sample(@RequestBody sample: Sample): Sample {
        logger.info(sample.toString())
        return sample
    }

    @GetMapping
    fun sample(): Sample {
        val sample = Sample("yun", 10)
        logger.info(sample.toString())
        return sample
    }
}

data class Sample(
        val name: String,
        val age: Int
)