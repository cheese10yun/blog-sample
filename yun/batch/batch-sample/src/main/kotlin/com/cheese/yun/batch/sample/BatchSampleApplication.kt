package com.cheese.yun.batch.sample

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableBatchProcessing
class BatchSampleApplication

fun main(args: Array<String>) {
    runApplication<BatchSampleApplication>(*args)
}