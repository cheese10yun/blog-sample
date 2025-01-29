package com.example.mongostudy.member

import com.example.mongostudy.MongoDataSetup
import com.example.mongostudy.MongoStudyApplicationTests
import com.example.mongostudy.MongoTestSupport
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.function.Consumer
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
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
        val map = (1..100).map {
            Member(
                name = "$it-name",
                address = Address(
                    address = "address",
                    addressDetail = "addressDetail",
                    zipCode = "zipCode",
                ),
                memberId = "memberId",
                email = "$it-asd@asd.com",
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

    @Test
    fun `applyPagination test`() {

        val members = memberRepository.findSlice(
            pageable = PageRequest.of(1, 10),
            name = null,
            email = null,
            memberId = "memberId"
        )

        members.content.forEach {
            println(it.name)
        }
    }

    @Test
    fun `findSlicePAggregation test`() {
        memberRepository.findSliceAggregation(
            pageable = PageRequest.of(0, 10),
//            name = null,
            name = "11-name",
//            email = null,
            email = "11-asd@asd.com",
//            memberId = "memberId",
            memberId = "memberId",
        ).content.forEach {
            println(it)
        }
    }

    @Test
    fun `findPage test`() {
        val members = memberRepository.findPage(
            pageable = PageRequest.of(1, 10),
            name = null,
            email = null,
            dateJoinedFrom = null,
            dateJoinedTo = null,
            memberStatus = null,
        )

        members.content.forEach {
            println(it.name)
        }
    }

    @Test
    fun `findPageAggregation test`() {
        val members = memberRepository.findPageAggregation(
            pageable = PageRequest.of(1, 10),
            name = null,
            email = null,
            memberId = "memberId",
        )

        println("number: ${members.number}")
        println("size: ${members.size}")
        println("totalPages: ${members.totalPages}")
        println("totalElements: ${members.totalElements}")

        members.content.forEach {
            println(it.name)
        }
    }
}