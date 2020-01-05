package com.dataflow.sample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SampleTaskApplication {
}

fun main(args: Array<String>) {
    runApplication<SampleTaskApplication>(*args)
}