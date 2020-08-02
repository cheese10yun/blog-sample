package com.spring.cloud.organization

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OrganizationServiceApplication

fun main(args: Array<String>) {
    runApplication<OrganizationServiceApplication>(*args)
}
