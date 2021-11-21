package com.example.unittest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UnitTestApplication

fun main(args: Array<String>) {
    runApplication<UnitTestApplication>(*args)
}
