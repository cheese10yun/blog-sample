package com.example.kafkasample

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ReceiveConfiguration {


    @KafkaListener(topics = ["test"], groupId = "console-consumer-1970")
    fun receive(payload: String) {

        println("received payload='{$payload}'")
    }

}
