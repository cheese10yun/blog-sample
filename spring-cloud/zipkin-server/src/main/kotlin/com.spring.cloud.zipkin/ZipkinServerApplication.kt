package com.spring.cloud.zipkin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import zipkin2.server.internal.EnableZipkinServer

@SpringBootApplication
@EnableZipkinServer
class ZipkinServerApplication

fun main(args: Array<String>) {
    runApplication<ZipkinServerApplication>(*args)
}