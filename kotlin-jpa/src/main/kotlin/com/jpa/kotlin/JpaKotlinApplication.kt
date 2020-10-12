package com.jpa.kotlin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JpaKotlinApplication

fun main(args: Array<String>) {
    runApplication<JpaKotlinApplication>(*args)
}
