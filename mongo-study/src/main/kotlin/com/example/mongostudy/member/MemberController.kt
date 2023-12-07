package com.example.mongostudy.member

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime

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
        @RequestParam(name = "dateJoinedFrom", required = false) dateJoinedFrom: LocalDateTime?,
        @RequestParam(name = "dateJoinedTo", required = false) dateJoinedTo: LocalDateTime?,
        @RequestParam(name = "memberStatus", required = false) memberStatus: MemberStatus?
    ): Page<Member> {
        return memberQueryService.findPageBy(
            pageable = pageable,
            name = name,
            email = email,
            dateJoinedFrom = dateJoinedFrom,
            dateJoinedTo = dateJoinedTo,
            memberStatus = memberStatus,
        )
    }

    @GetMapping("/name")
    fun findByName(
        @RequestParam(name = "name") name: String
    ): List<Member> {
        return memberRepository.findByName(name)
    }

    @GetMapping("/test")
    fun findByName() {
        val map = (1..100_000).map {
            Member(
                memberId = "memberId-$it",
                name = "name-$it",
                email = "email-$it",
                dateJoined = LocalDateTime.now(),
                dateOfBirth = LocalDate.now(),
                phoneNumber = "phoneNumber-$it",
                address = Address(
                    address = "address-$it",
                    addressDetail = "addressDetail-$it",
                    zipCode = "zipCode-$it"
                ),
                status = MemberStatus.ACTIVE,
                pointsAccumulated = it.toBigDecimal(),
                lastPurchaseDate = LocalDateTime.now()
            )
        }

        memberRepository.insert(map)
    }
}