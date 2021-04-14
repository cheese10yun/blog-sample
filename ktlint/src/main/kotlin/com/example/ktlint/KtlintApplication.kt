package com.example.ktlint

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KtlintApplication

fun main(args: Array<String>) {
    runApplication<KtlintApplication>(*args)
}
