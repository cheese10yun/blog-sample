package com.dataflow.sample

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.task.configuration.EnableTask
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableTask
class SampleTaskApplication

fun main(args: Array<String>) {
    runApplication<SampleTaskApplication>(*args)
}

@Component
class Sample : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {

        val sourceArgs = args?.sourceArgs
        val args = sourceArgs?.get(0)

        println("Hello, World! $args" )
    }

}