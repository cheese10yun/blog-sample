package com.example.redis

import com.zaxxer.hikari.HikariDataSource
import io.lettuce.core.metrics.DefaultCommandLatencyCollectorOptions
import io.lettuce.core.resource.ClientResources
import javax.sql.DataSource
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
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

    @OptIn(FlowPreview::class)
     fun getRedis(id: String): Any? {
        printLettuceConnection()
//        val existsByCode = couponRepository.existsByCode("CODE-$id")
//        val findByCode = couponRepository.findByCode("CODE-$id")
//        val findByIdOrNull = couponRepository.findByIdOrNull(id)
        val ids = (1..50).map { it.toString() }

        val runBlocking = runBlocking {

            ids
                .asFlow()
                .flatMapMerge { it ->
                    flow {
                        emit(couponRepository.findByIdOrNull(it))
                    }
                }
                .toList()
        }

        return  runBlocking


//        couponRepository.findAllById(ids)
//        couponRepository.findAllById(ids)
//        couponRepository.findAllById(ids)
//        couponRepository.findAllById(ids)
//        couponRepository.findAllById(ids)
//

//        val a1 = couponRepository.findByDiscountGreaterThan(id.toDouble())
//        val a2 = couponRepository.findByDiscount(id.toDouble())
//        val a3 = couponRepository.findByDiscountBetween(id.toDouble(), id.toDouble())
//        val a4 = couponRepository.findByValid()
//        val a5 = couponRepository.findByCodeStartingWith("CODE-$id")
//        val a6 = couponRepository.findByCodeEndingWith("CODE-$id")
//        val a7 = couponRepository.findByCodeContaining("CODE-$id")
//        val a8 = couponRepository.findByIdIn(listOf(id))
//        val a9 = couponRepository.findByValidAndDiscountGreaterThan(true, id.toDouble())
//        val a10 = couponRepository.existsByValidAndDiscountLessThan(true, id.toDouble())
//        val a11 = couponRepository.findCouponsByDiscountGreaterThan(id.toDouble())


//        val a12 = couponRepository.findCouponByCode("CODE-$id")

//            return couponRepository.findByIdOrNull(id)
//        couponRepository.findAllById(ids)
    }

//    fun get(id: String): Pair<Coupon?, Order?> {
//        val coupon = couponRepository.findByIdOrNull(id)
//        val ids = (1..100).map { it.toString() }
//        couponRepository.findAllById(ids)
//        val order = orderRepository.findByIdOrNull(id.toLong())
//        Thread.sleep(2500)
////        runBlocking { delay(2500) }
//
////        printLettuceConnection()
////        printHikariConnection()
//
//        return Pair(coupon, order)
//    }

    fun getComposite(id: String): Pair<Coupon?, Order?> {
        val coupon = couponRepository.findByIdOrNull(id)
        val order = orderRepository.findByIdOrNull(id.toLong())
        Thread.sleep(2500)
//        runBlocking { delay(2500) }
        printLettuceConnection()
        printHikariConnection()

        return Pair(coupon, order)
    }

    fun getMySql(id: Long): Order? {
        printHikariConnection()
        return orderRepository.findByIdOrNull(id)
    }


    private fun printHikariConnection() {
        val targetDataSource = dataSource.unwrap(HikariDataSource::class.java)
        val hikariDataSource = targetDataSource as HikariDataSource
        val hikariPoolMXBean = hikariDataSource.hikariPoolMXBean
        val hikariConfigMXBean = hikariDataSource.hikariConfigMXBean
        hikariConfigMXBean.minimumIdle

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