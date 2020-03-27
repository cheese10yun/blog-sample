package com.example.kafkasamplenew

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class Consumer {

    @KafkaListener(topics = ["new"], groupId = "eddy")
    fun listener(message: String) {

        println("================================")
        println("smile: $message")
        println("================================")
    }
}