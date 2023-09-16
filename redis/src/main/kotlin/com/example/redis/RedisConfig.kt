package com.example.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.listener.RedisMessageListenerContainer


@Configuration
class RedisConfig {

//    @Bean
//    fun redisConnectionFactory(): RedisConnectionFactory {
//        return LettuceConnectionFactory()
//    }
//
//    @Bean
//    fun redisContainer(
//        redisConnectionFactory: RedisConnectionFactory
//    ): RedisMessageListenerContainer {
//        val container = RedisMessageListenerContainer()
//        container.setConnectionFactory(redisConnectionFactory)
//        return container
//    }

//    @Bean
//    fun redisConnectionFactory(): RedisConnectionFactory {
//        return LettuceConnectionFactory()
//    }

//    @Bean
//    fun redisContainer(): RedisMessageListenerContainer {
//        val container = RedisMessageListenerContainer()
//        container.setConnectionFactory(redisConnectionFactory())
//        return container
//    }


//    @Bean
//    @Primary
//    fun redisTemplate(redisConnectionFactory: LettuceConnectionFactory): RedisTemplate<*, *> =
//        RedisTemplate<ByteArray, ByteArray>().apply {
//            setConnectionFactory(redisConnectionFactory)
//        }
//            .apply {
//                this.hashValueSerializer
//            }
//
//    @Bean
//    fun redisTemplateWithTransaction(redisConnectionFactory: LettuceConnectionFactory): StringRedisTemplate {
//        val template = StringRedisTemplate(redisConnectionFactory)
//        // explicitly enable transaction support
//        template.setEnableTransactionSupport(true)
//        return template
//    }


//    @Bean
//    @Primary
//    fun redisConnectionFactory(): LettuceConnectionFactory =
//        LettuceConnectionFactory(
//            RedisStandaloneConfiguration("localhost", 6379)
//                .apply { password = RedisPassword.of("root") }
//        ).apply {
//            setPipeliningFlushPolicy(
//                LettuceConnection.PipeliningFlushPolicy.buffered(1000)
//            )
//        }

}