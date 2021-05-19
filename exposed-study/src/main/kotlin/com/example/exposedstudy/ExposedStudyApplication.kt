package com.example.exposedstudy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
class ExposedStudyApplication

fun main(args: Array<String>) {
	runApplication<ExposedStudyApplication>(*args)
}
