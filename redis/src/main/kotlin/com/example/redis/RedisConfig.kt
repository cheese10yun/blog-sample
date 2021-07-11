package com.example.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnection
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

@Configuration
class RedisConfig {


    @Bean
    @Primary
    fun redisTemplate(redisConnectionFactory: LettuceConnectionFactory): RedisTemplate<ByteArray, ByteArray> =
        RedisTemplate<ByteArray, ByteArray>().apply {
            setConnectionFactory(redisConnectionFactory)
        }


    @Bean
    @Primary
    fun redisConnectionFactory(): LettuceConnectionFactory =
        LettuceConnectionFactory(
            RedisStandaloneConfiguration("localhost", 6379)
                .apply { password = RedisPassword.of("root") }
        ).apply {
            setPipeliningFlushPolicy(
                LettuceConnection.PipeliningFlushPolicy.buffered(1000)
            )
        }

}