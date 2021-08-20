package com.example.redis

import java.util.UUID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor

@SpringBootTest
@ActiveProfiles("local")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RedisApplicationTests(
    val memberRepository: MemberRepository,
//    val redisTemplate: RedisTemplate<ByteArray, ByteArray>,
    val redisTemplate: RedisTemplate<String, Member>,
    val memberBulkInsertService: MemberBulkInsertService,
    val redisTemplateWithTransaction: StringRedisTemplate,
    val syncRedisRepository: SyncRedisRepository,
    val syncPayoutGroupHashRepository: SyncPayoutGroupHashRepository
) {

    private val memberCount = 1000

    @Test
    fun `members 저장 테스트`() {
        val members = (1..memberCount).map {
            Member(
                id = it.toLong(),
                ttl = 20L
            )
        }
        memberRepository.saveAll(members)
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

    @Test
    fun adasdasdasd() {
        val map = (1..5).map {
            SyncRedis(
                id = it + 1.toLong(),
                payoutGroupId = it.toLong(),
                groupId = it.toString(),
                partnerId = it.toLong()
            )
        }

        syncRedisRepository.saveAll(map)

        val findByPayoutGroupIdAndGroupId = syncRedisRepository.findByPayoutGroupIdAndGroupId(
            payoutGroupId = 1L,
            groupId = "123123"
        )
        println(findByPayoutGroupIdAndGroupId)
    }
}