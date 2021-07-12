package com.example.redis

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.context.TestConstructor
import org.springframework.util.StopWatch

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RedisApplicationTests(
    val memberRepository: MemberRepository,
//    val redisTemplate: RedisTemplate<ByteArray, ByteArray>,
    val redisTemplate: RedisTemplate<String, Member>,
    val memberBulkInsertService: MemberBulkInsertService,
    val redisTemplateWithTransaction: StringRedisTemplate
) {

    private val memberCount = 5000

    @Test
    fun `members`() {
        val members = (1..memberCount).map {
            Member(it.toLong())
        }
        val stopWatch = StopWatch()
        stopWatch.start()
        memberRepository.saveAll(members)
        stopWatch.stop()


        println(stopWatch.lastTaskTimeMillis)
//        memberRepository.deleteAll()

    }

    @Test
    fun `bulk transaction test`() {

        val members = (1..memberCount).map {
            Member(it.toLong())
        }

        memberBulkInsertService.save(members)
    }

//    @Test
//    fun `pipe line`() {
//        val members = (1..memberCount).map {
//            Member(it.toLong())
//        }
//
//        val stopWatch = StopWatch()
//        stopWatch.start()
//
//        redisTemplate.execute { rc ->
//            rc.openPipeline()
//            members.forEach { member ->
//                member.toString().toByteArray() to member.toString().toByteArray()
//                val mutableMap = mutableMapOf<ByteArray, ByteArray>()
//                val toByteArray = "member-${member.id}".toByteArray()
//                mutableMap[toByteArray] = member.toString().toByteArray()
//                rc.hMSet(toByteArray, mutableMap)
//            }
//            rc.closePipeline()
//        }!!
//
//        stopWatch.stop()
//        println(stopWatch.lastTaskTimeMillis)
//        memberRepository.deleteAll()
//
//
//        redisTemplate.execute {
//            it.flushAll()
//        }
//    }

    @Test
    fun `pipe line`() {

        val members = (1..memberCount).map {
            Member(it.toLong())
        }

        redisTemplate.execute { rc ->
            rc.openPipeline()

        }
    }

    @Test
    fun asdasdasd() {
        // executed on thread bound connection
        redisTemplateWithTransaction.opsForValue().set("foo", "bar");

// read operation executed on a free (not tx-aware)
        redisTemplateWithTransaction.keys("*");

// returns null as values set within transaction are not visible
        redisTemplateWithTransaction.opsForValue().get("foo");
    }
}

// 37352