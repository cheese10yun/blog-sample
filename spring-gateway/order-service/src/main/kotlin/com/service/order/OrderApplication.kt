package com.service.order

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OrderApplication

fun main(args: Array<String>) {
    runApplication<OrderApplication>(*args)
}