package com.example.kotlinjunit5

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class AppRunner(private val sampleProperties: SampleProperties) :ApplicationRunner{


    override fun run(args: ApplicationArguments?) {

        println("=====================")
        println(sampleProperties.toString())
        println("=====================")


        println("==================")
        println(Sample.Properties.email)
        println("==================")


    }
}