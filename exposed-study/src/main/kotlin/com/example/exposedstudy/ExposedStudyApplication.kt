package com.example.exposedstudy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ExposedStudyApplication

fun main(args: Array<String>) {
    runApplication<ExposedStudyApplication>(*args)
}