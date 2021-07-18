package com.service.order

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableFeignClients
class OrderApplication

fun main(args: Array<String>) {
    runApplication<OrderApplication>(*args)
}

@Component
class OrderApplicationRunner(
    private val orderRepository: OrderRepository
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        (1..10).map {
            orderRepository.save(Order(it.toLong()))
        }
    }
}