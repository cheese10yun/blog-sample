package com.example.redis

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.TestConstructor
import org.springframework.util.StopWatch

@SpringBootTest
//@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RedisApplicationTests(
//    val memberRepository: MemberRepository,
    val redisTemplate: RedisTemplate<ByteArray, ByteArray>
) {

    @Test
    fun `members`() {
        val members = (1..10_000).map {
            Member(it.toLong())
        }
        val stopWatch = StopWatch()
        stopWatch.start()
//        memberRepository.saveAll(members)
        stopWatch.stop()

        redisTemplate.execute { rc ->
            rc.openPipeline()
            members.forEach { member ->

                member.toString().toByteArray() to member.toString().toByteArray()
                val mutableMap = mutableMapOf<ByteArray, ByteArray>()

                val toByteArray = "member-${member.id}".toByteArray()

                mutableMap[toByteArray] = member.toString().toByteArray()


                rc.hMSet(toByteArray, mutableMap)
            }
            rc.closePipeline()
        }!!


        println(stopWatch.lastTaskTimeMillis)
        // 1,000 = 66811
        // 1 = 62877
        // 46757

//        memberRepository.deleteAll()

    }
}