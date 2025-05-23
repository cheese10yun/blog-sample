package com.example.redis

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
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
//    private val chatService: ChatService,
    private val memberRepository: MemberRepository,
    private val addressRepository: AddressRepository,
    private val couponRepository:CouponRepository,
    private val orderRepository: OrderRepository
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        saveOrder()
        saveCoupon()
//        saveMember()
//        saveAddress()
    }

    private fun saveOrder() {
        val orders = (1..10).map {
            Order(
                productName = "product-${it}",
                quantity = it,
                price = 100.0
            )
        }

        orderRepository.saveAll(orders)
    }

    private fun saveCoupon() {
        val coupons = (1..100).map {
            Coupon(
                id = it.toString(),
                discount = 0.1,
                code = "CODE-${it}",
                valid = true,
            )
        }
        couponRepository.saveAll(coupons)
    }

    private fun saveMember() {
        val members = (1..100).map {
            Member(
                id = it.toString(),
                ttl = 1000L,
            )
        }
        memberRepository.saveAll(members)
    }

    private fun saveAddress() {
        val addresses = (1..100).map {
            Address(
                street = "moderatius-${it}",
                city = "Noordeloos-${it}",
                state = "Rhode Island-${it}",
                zipcode = "61969-${it}",
            )
        }
        addressRepository.saveAll(addresses)
    }
}

//@Service
//class ChatService(
//    private val container: RedisMessageListenerContainer,
//    private val redisTemplate: RedisTemplate<String, String>
//) : MessageListener {
//
//    fun enterChatRoom(chatRoomName: String) {
//        container.addMessageListener(this, ChannelTopic(chatRoomName))
//
//        val scanner = Scanner(System.`in`)
//        while (scanner.hasNextLine()) {
//            val line = scanner.next()
//            if (line == "q") {
//                println("Quit....")
//                break
//            } else {
//                redisTemplate.convertAndSend(chatRoomName, line)
//            }
//        }
//    }
//
//
//    override fun onMessage(message: Message, pattern: ByteArray?) {
//        println("Message $message")
//    }
//}