package com.batch.study

import com.batch.study.job.reader.ReaderPerformanceJobProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableBatchProcessing
@EnableConfigurationProperties(ReaderPerformanceJobProperties::class)
class StudyApplication

fun main(args: Array<String>) {
    runApplication<StudyApplication>(*args)
}

fun <A : Any> A.logger(): Lazy<Logger> = lazy { LoggerFactory.getLogger(this.javaClass) }