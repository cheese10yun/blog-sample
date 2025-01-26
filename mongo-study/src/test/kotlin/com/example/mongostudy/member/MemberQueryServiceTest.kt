package com.example.mongostudy.member

import com.example.mongostudy.MongoDataSetup
import com.example.mongostudy.MongoStudyApplicationTests
import com.example.mongostudy.MongoTestSupport
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.function.Consumer
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.findAll
import org.springframework.util.StopWatch

@MongoTestSupport
class MemberRepositoryTest(
    private val memberRepository: MemberRepository
) : MongoStudyApplicationTests() {

    @MongoDataSetup(
        jsonPath = "/",
        clazz = Member::class
    )
    @Test
    fun `findByEmail asd`() {
        //given
        memberRepository.findByEmail("sample@test.com")
        //when
    }

    @Test
    fun `updateName test`() {
        // given
        val map = (1..20).map {
            Member(
                name = "name",
                address = Address(
                    address = "address",
                    addressDetail = "addressDetail",
                    zipCode = "zipCode",
                ),
                memberId = "memberId",
                email = "asd@asd.com",
                status = MemberStatus.ACTIVE,
                pointsAccumulated = BigDecimal.ONE,
                dateJoined = LocalDateTime.now()

            )
        }

        val targets = mongoTemplate
            .insertAll(map).map {
                MemberQueryForm.UpdateName(
                    id = it.id!!,
                    name = "newName"
                )
            }

        // when
        memberRepository.updateName(targets)

        // then
        val results = mongoTemplate.findAll<Member>()

        then(results).hasSize(20)
        then(results).allSatisfy(
            Consumer {
                then(it.name).isEqualTo("newName")
            }
        )
    }

    @Test
    fun `insertAll`() {
        // given
        val map = (1..100_000).map {
            Member(
                name = "name",
                address = Address(
                    address = "address",
                    addressDetail = "addressDetail",
                    zipCode = "zipCode",
                ),
                memberId = "memberId",
                email = "asd@asd.com",
                status = MemberStatus.ACTIVE,
                pointsAccumulated = BigDecimal.ONE,
                dateJoined = LocalDateTime.now()
            )
        }

        val stopWatch = StopWatch()

        stopWatch.start()
        memberRepository.insertMany(map)
        stopWatch.stop()


        println("${map.size}: ${stopWatch.totalTimeMillis}")

//        println(stopWatch.prettyPrint())
    }

    @Test
    fun `saveAll`() {
        // given
        val map = (1..100_000).map {
            Member(
                name = "name",
                address = Address(
                    address = "address",
                    addressDetail = "addressDetail",
                    zipCode = "zipCode",
                ),
                memberId = "memberId",
                email = "asd@asd.com",
                status = MemberStatus.ACTIVE,
                pointsAccumulated = BigDecimal.ONE,
                dateJoined = LocalDateTime.now()
            )
        }

        val stopWatch = StopWatch()

        stopWatch.start()
        memberRepository.saveAll(map)
        stopWatch.stop()

        println("${map.size}: ${stopWatch.totalTimeMillis}")

//        println(stopWatch.prettyPrint())
    }
}