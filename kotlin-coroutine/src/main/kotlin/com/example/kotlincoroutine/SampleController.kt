package com.example.kotlincoroutine

import com.zaxxer.hikari.HikariDataSource
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.sql.DataSource
import kotlin.random.Random
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class SampleController(
    private val dataSource: DataSource,
    private val memberRepository: MemberRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)!!

    @GetMapping("/api/v1/members")
    fun sample(): Member {
        // 1 ~ 100 사이의 랜덤으로 member 조회
        val findById = memberRepository.findById(Random.nextInt(1, 101).toLong()).get()

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
            connectionTimeout : ${hikariConfigMXBean.connectionTimeout}
            validationTimeout : ${hikariConfigMXBean.validationTimeout}
            idleTimeout : ${hikariConfigMXBean.idleTimeout}
            """.trimIndent()

        this.log.info(log)

        return findById
    }
}

@Component
class Runner(
    private val memberRepository: MemberRepository
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        (1..100).forEach {
            memberRepository.save(Member(name = "name$it", age = it))
        }
    }
}

@Entity(name = "member")
@Table(name = "member")
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    val name: String,
    val age: Int
)

interface MemberRepository : JpaRepository<Member, Long>
