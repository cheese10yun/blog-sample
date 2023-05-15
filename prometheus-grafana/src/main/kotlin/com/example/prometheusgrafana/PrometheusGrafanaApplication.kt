package com.example.prometheusgrafana

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PrometheusGrafanaApplication

fun main(args: Array<String>) {
    runApplication<PrometheusGrafanaApplication>(*args)
}
