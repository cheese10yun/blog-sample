package com.spring.cloud.zuul

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.zuul.EnableZuulProxy

@SpringBootApplication
@EnableZuulProxy
class ZuulServerApplication

fun main(args: Array<String>) {
    runApplication<ZuulServerApplication>(*args)
}