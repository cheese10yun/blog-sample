package com.example.mongostudy.member

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/members")
class MemberController(
    private val memberQueryService: MemberQueryService
) {

    @GetMapping
    fun findPageBy(
        @PageableDefault pageable: Pageable,
        @RequestParam(name = "name", required = false) name: String?,
        @RequestParam(name = "email", required = false) email: String?,
        @RequestParam(name = "dateJoinedFrom", required = false) dateJoinedFrom: LocalDateTime?,
        @RequestParam(name = "dateJoinedTo", required = false) dateJoinedTo: LocalDateTime?,
        @RequestParam(name = "membershipStatus", required = false) membershipStatus: MembershipStatus?
    ): Page<Member> {
        return memberQueryService.findPageBy(
            pageable = pageable,
            name = name,
            email = email,
            dateJoinedFrom = dateJoinedFrom,
            dateJoinedTo = dateJoinedTo,
            membershipStatus = membershipStatus,
        )
    }
}