package com.example.kafkasample

import org.springframework.http.ResponseEntity
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class KafkaCtrl(private val kafkaTemplate: KafkaTemplate<String, String>) {

    @PostMapping("/send")
    fun sendMessage(message: String): ResponseEntity<String> {
        if (!StringUtils.isEmpty(message)) {
            kafkaTemplate.send("test", "Message is $message")
        }
        println(message)
        return ResponseEntity.ok("")
    }
}


