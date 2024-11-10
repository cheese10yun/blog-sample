package com.example.redis

import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service


@Service
class RedisConnectionPoolSample(
    private val couponRepository: CouponRepository,
    private val orderRepository: OrderRepository,
    private val lettuceConnectionFactory: LettuceConnectionFactory,
    private val dataSource: DataSource

) {

    fun get2(id: String): Boolean {
        printLettuceConnection()
        couponRepository.existsByCode("CODE-$id")
        couponRepository.findByCode("CODE-$id")
        couponRepository.findByIdOrNull("CODE-$id")

        return couponRepository.existsByCode("CODE-$id")
    }

    fun get(id: String): Pair<Boolean, Order?> {
        val coupon = couponRepository.existsByCode("CODE-$id")
        val order = orderRepository.findByIdOrNull(id.toLong())
        Thread.sleep(2500)
//        runBlocking { delay(2500) }

        printLettuceConnection()
//        printHikariConnection()

        return Pair(coupon, order)
    }


    private fun printHikariConnection(){
        val targetDataSource = dataSource.unwrap(HikariDataSource::class.java)
        val hikariDataSource = targetDataSource as HikariDataSource
        val hikariPoolMXBean = hikariDataSource.hikariPoolMXBean
        val hikariConfigMXBean = hikariDataSource.hikariConfigMXBean

        val log = buildString {
            append("totalConnections: ${hikariPoolMXBean.totalConnections}, ")
            append("activeConnections: ${hikariPoolMXBean.activeConnections}, ")
            append("idleConnections: ${hikariPoolMXBean.idleConnections}, ")
            append("threadsAwaitingConnection: ${hikariPoolMXBean.threadsAwaitingConnection}")
        }
        println(log)
    }

    private fun printLettuceConnection() {
        val pool = (lettuceConnectionFactory.clientConfiguration as LettucePoolingClientConfiguration).poolConfig
        val log = buildString {
            append("maxTotal: ${pool.maxTotal}, ")
            append("maxIdle: ${pool.maxIdle}, ")
            append("minIdle: ${pool.minIdle}, ")
        }
        println(log)
    }
}