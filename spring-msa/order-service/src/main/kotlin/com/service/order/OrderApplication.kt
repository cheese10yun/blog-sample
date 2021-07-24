package com.service.order

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableFeignClients
@RefreshScope
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

fun <A : Any> A.logger(): Lazy<Logger> = lazy { LoggerFactory.getLogger(this.javaClass) }