package com.batch.task

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableBatchProcessing
class CsvReaderApplication

fun main(args: Array<String>) {
    runApplication<CsvReaderApplication>(*args)
}