package com.example.mongostudy.member

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MemberQueryService(
    private val memberRepository: MemberRepository
) {

    fun findPageBy(
        pageable: Pageable,
        name: String?,
        email: String?,
        dateJoinedFrom: LocalDateTime?,
        dateJoinedTo: LocalDateTime?,
        membershipStatus: MembershipStatus?
    ): Page<Member> {
        return memberRepository.findPageBy(
            pageable = pageable,
            name = name,
            email = email,
            dateJoinedFrom = dateJoinedFrom,
            dateJoinedTo = dateJoinedTo,
            membershipStatus = membershipStatus
        )
    }
}