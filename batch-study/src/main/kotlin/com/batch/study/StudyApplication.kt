package com.batch.study

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StudyApplication

fun main(args: Array<String>) {
    runApplication<StudyApplication>(*args)
}
