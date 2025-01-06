package com.example.mongostudy

import com.example.mongostudy.member.Address
import com.example.mongostudy.member.Member
import com.example.mongostudy.member.MemberStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random
import org.junit.jupiter.api.Test
import org.springframework.util.StopWatch

class UpsertTest : MongoStudyApplicationTests() {

    @Test
    fun `saveAll Test`() {
        val stopWatch = StopWatch()
        val members = (1..50).map {
            Member(
                memberId = UUID.randomUUID().toString(),
                name = "Member $it",
                email = "member$it@example.com",
                dateJoined = LocalDateTime.now(),
                address = Address(
                    address = "address $it",
                    addressDetail = "address detail $it",
                    zipCode = "zip code - $it"
                ),
                status = MemberStatus.values()[Random.nextInt(MemberStatus.values().size)],
                pointsAccumulated = BigDecimal(Random.nextInt(1000)),
            )
        }

        mongoTemplate.insertAll(members)
        stopWatch.start()
        stopWatch.stop()
        stopWatch.prettyPrint()
    }
}