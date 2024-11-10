package com.example.redis

import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import kotlin.jvm.optionals.getOrNull
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class MemberController(
    private val memberRepository: MemberRepository,
    private val addressRepository: AddressRepository,
    private val dataSource: DataSource,
    private val redisConnectionPoolSample: RedisConnectionPoolSample,
) {

    @GetMapping("/api/redis")
    fun getRedis(
        @RequestParam("id") id: String
    ) = redisConnectionPoolSample.get2(id)

    @GetMapping("/api/composite")
    fun getRedis2(
        @RequestParam("id") id: String
    ) = redisConnectionPoolSample.get(id)


    @GetMapping("/api/members")
    fun getMember(): Member? {
        memberRepository.findById(1).getOrNull()
        memberRepository.findById(35).getOrNull()
        return memberRepository.findById(42).getOrNull()
    }

    @GetMapping("/api/address")
    fun getAddress(): List<Address> {
        val targetDataSource = dataSource.unwrap(HikariDataSource::class.java)
        val hikariDataSource = targetDataSource as HikariDataSource
        val hikariPoolMXBean = hikariDataSource.hikariPoolMXBean
        val hikariConfigMXBean = hikariDataSource.hikariConfigMXBean
        val log =
            """
            totalConnections : ${hikariPoolMXBean.totalConnections}
            activeConnections : ${hikariPoolMXBean.activeConnections}
            idleConnections : ${hikariPoolMXBean.idleConnections}
            threadsAwaitingConnection : ${hikariPoolMXBean.threadsAwaitingConnection}
            maxLifetime : ${hikariConfigMXBean.maxLifetime}
            maximumPoolSize : ${hikariConfigMXBean.maximumPoolSize}
            minimumIdle : ${hikariConfigMXBean.minimumIdle}
            connectionTimeout : ${hikariConfigMXBean.connectionTimeout}
            validationTimeout : ${hikariConfigMXBean.validationTimeout}
            idleTimeout : ${hikariConfigMXBean.idleTimeout}
            """.trimIndent()
        return addressRepository.findAll().toList()
    }
}