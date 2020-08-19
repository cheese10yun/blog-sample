package com.service.order

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class OrderApplication

fun main(args: Array<String>) {
    runApplication<OrderApplication>(*args)
}