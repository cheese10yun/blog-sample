package com.example.steam

import com.example.steam.model.OrganizationChangeModel
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.cloud.stream.messaging.Source

@SpringBootApplication
//@EnableBinding(Source::class)
@EnableBinding(Sink::class)
class SteamApplication

fun main(args: Array<String>) {
    runApplication<SteamApplication>(*args)
}

@StreamListener(Sink.INPUT)
fun loggerSink(model: OrganizationChangeModel){
    println("Received an event for : $model")
}
