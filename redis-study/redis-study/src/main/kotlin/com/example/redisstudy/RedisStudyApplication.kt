package com.example.redisstudy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RedisStudyApplication

fun main(args: Array<String>) {
	runApplication<RedisStudyApplication>(*args)
}
