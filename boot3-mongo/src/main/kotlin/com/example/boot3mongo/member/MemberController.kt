package com.example.boot3mongo.member

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.web.PageableDefault
import org.springframework.util.StopWatch
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/v1/members")
class MemberController(
    private val memberQueryService: MemberQueryService,
    private val memberRepository: MemberRepository
) {

    @GetMapping
    fun findPageBy(
        @PageableDefault pageable: Pageable,
        @RequestParam(name = "name", required = false) name: String?,
        @RequestParam(name = "email", required = false) email: String?,
        @RequestParam(name = "memberId", required = false) memberId: String?,
    ): Page<Member> {
        return memberQueryService.findPageBy(
            pageable = pageable,
            name = name,
            email = email,
            memberId = memberId,
        )
    }

    @GetMapping("/name")
    fun findByName(
        @RequestParam(name = "name") name: String
    ): List<Member> {
        return memberRepository.findByName(name)
    }


    @GetMapping("/insert")
    fun findByName() {
        val map = (1..100_000).map {
            Member(
                memberId = "memberId-$it",
                name = "name-$it",
                email = "email-$it",
                dateJoined = LocalDateTime.now(),
                address = Address(
                    address = "address-$it",
                    addressDetail = "addressDetail-$it",
                    zipCode = "zipCode-$it"
                ),
                status = MemberStatus.ACTIVE,
                pointsAccumulated = it.toBigDecimal(),
            )
        }

        memberRepository.insert(map)
    }

    @GetMapping("/update/bulk")
    fun updateBulk(
        @RequestParam count: Int,
        @RequestParam name: String,
    ): Double {

        val members = memberRepository
            .findAll(PageRequest.of(0, count))
            .content
            .filterNotNull()


        val loop = 10
        val map = (1..loop).map {
            members.map {
                Pair(
                    first = { Query(Criteria.where("_id").`is`(it.id!!)) },
                    second = { Update().set("name", UUID.randomUUID().toString()) }
                )
            }
        }


        val timeTaken = emptyList<Long>().toMutableList()
        (1..loop).forEachIndexed { index, _ ->
            val stopWatch = StopWatch()
            stopWatch.start()
//            memberQueryService.updateBulkTest(map[index], BulkOperations.BulkMode.UNORDERED)
            stopWatch.stop()
            val element = stopWatch.totalTimeMillis
//            println("============")
//            println("index:${index} bulk size: ${members.size}, $element ms")
//            println("============")
            timeTaken.add(element)
        }

        val average = timeTaken.average()
        println("average $average")
        return average
    }


    @GetMapping("/update")
    fun update(
        @RequestParam count: Int,
        @RequestParam name: String,
    ): Double {
        val findAll = memberRepository.findAll(PageRequest.of(0, count))

        val loop = 10
        val members = findAll.content
            .filterNotNull()

        val map = (1..loop).map {
            members.forEach {
//                it.name = UUID.randomUUID().toString()
            }
            members
        }

        val timeTaken = emptyList<Long>().toMutableList()
        (1..loop).forEachIndexed { index, _ ->
            val stopWatch = StopWatch()
            stopWatch.start()
            memberQueryService.update(map[index])
            stopWatch.stop()
            val element = stopWatch.totalTimeMillis
//            println("============")
//            println("index:${index} bulk size: ${members.size}, $element ms")
//            println("============")
            timeTaken.add(element)
        }


        val average = timeTaken.average()
        println("average $average")
        return average
    }

    @GetMapping("/update/updateFirst")
    fun updateFirst(
        @RequestParam count: Int,
        @RequestParam name: String,
    ): Double {
        val findAll = memberRepository.findAll(PageRequest.of(0, count))

        val loop = 10
        val members = findAll.content
            .filterNotNull()

        val map = (1..loop).map {
            members.forEach {
//                it.name = UUID.randomUUID().toString()
            }
            members
        }

        val timeTaken = emptyList<Long>().toMutableList()
        (1..loop).forEachIndexed { index, _ ->
            val stopWatch = StopWatch()
            stopWatch.start()

            map[index].forEach {
                memberRepository.update(it.id!!)
            }

            stopWatch.stop()
            val element = stopWatch.totalTimeMillis
//            println("============")
//            println("index:${index} updateFirst size: ${members.size}, $element ms")
//            println("============")
            timeTaken.add(element)
        }


        val average = timeTaken.average()
        println("average $average")
        return average
    }
}