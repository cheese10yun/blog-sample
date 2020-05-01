package com.cheese.yun.api

import com.cheese.yun.domain.config.EnableDomain
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableDomain
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}

