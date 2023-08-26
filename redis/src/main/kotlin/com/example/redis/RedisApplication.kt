package com.example.redis

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Service
import java.util.Scanner

@SpringBootApplication
//@EnableCaching
class RedisApplication

fun main(args: Array<String>) {
    runApplication<RedisApplication>(*args)
}


@Configuration
class AppRunner(
    private val chatService: ChatService
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        println("Application Started...")
        chatService.enterChatRoom("chat1")
    }
}

@Service
class ChatService(
    private val container: RedisMessageListenerContainer,
    private val redisTemplate: RedisTemplate<String, String>
) : MessageListener {

    fun enterChatRoom(chatRoomName: String) {
        container.addMessageListener(this, ChannelTopic(chatRoomName))

        val scanner = Scanner(System.`in`)
        while (scanner.hasNextLine()) {
            val line = scanner.next()
            if (line == "q") {
                println("Quit....")
                break
            } else {
                redisTemplate.convertAndSend(chatRoomName, line)
            }
        }
    }


    override fun onMessage(message: Message, pattern: ByteArray?) {
        println("Message $message")
    }
}