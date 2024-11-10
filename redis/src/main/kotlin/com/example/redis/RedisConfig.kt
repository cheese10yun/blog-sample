package com.example.redis

import io.lettuce.core.internal.LettuceClassUtils
import io.lettuce.core.resource.NettyCustomizer
import io.netty.channel.Channel
import io.netty.resolver.AddressResolver
import io.netty.resolver.AddressResolverGroup
import io.netty.resolver.DefaultNameResolver
import io.netty.util.concurrent.EventExecutor
import java.net.InetSocketAddress
import org.apache.catalina.core.ApplicationContext
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnection
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.RedisMessageListenerContainer

class AddressResolverGroupCustomizer private constructor() : AddressResolverGroup<InetSocketAddress>() {


    var dnsResolverAvailable = LettuceClassUtils.isPresent("io.netty.resolver.dns.DnsAddressResolverGroup")


    override fun newResolver(p0: EventExecutor?): AddressResolver<InetSocketAddress> {

        if (dnsResolverAvailable) {

        }

        return DefaultNameResolver(p0).asAddressResolver()
    }

    companion object {
        val INSTANCE = AddressResolverGroupCustomizer()
    }
}

@Configuration
//@AutoConfiguration(before = [RedisAutoConfiguration::class])
class RedisConfig {

//    @Bean
//    fun redisConnectionFactory(): RedisConnectionFactory {
//        // LettuceConnectionFactory를 이용해 Redis 연결 설정
//        return LettuceConnectionFactory()
//    }


    @Bean
    fun redisTemplate(
        redisConnectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, Coupon> {
        val redisTemplate = RedisTemplate<String, Coupon>()
        redisTemplate.setConnectionFactory(redisConnectionFactory)
        return redisTemplate
    }


//    @Bean
//    fun redisTemplate(
//        redisConnectionFactory: RedisConnectionFactory
//    ): RedisTemplate<String, Any> {
//        val template = RedisTemplate<String, Any>()
//        template.setConnectionFactory(redisConnectionFactory)
//        return template
//    }

//    @Bean
//    fun clientResourcesCustomizer(ctx: ApplicationContext): ClientResourcesBuilderCustomizer {
//        return ClientResourcesBuilderCustomizer { builder ->
//
//            builder
//                .addressResolverGroup(AddressResolverGroupCustomizer.INSTANCE)
////                .nettyCustomizer(
////                    object : NettyCustomizer{
////                        override fun afterChannelInitialized(channel: Channel?) {
////
////                            channel.pipeline().addLast(
////
////                            )
////                        }
////                    }
////                )
//
//        }
//
//    }


//    @Bean
//    fun redisConnectionFactory(): RedisConnectionFactory {
//        return LettuceConnectionFactory()
//    }

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