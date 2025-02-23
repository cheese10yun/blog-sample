package com.example.boot3mongo.member

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class MemberQueryService(
    private val memberRepository: MemberRepository
) {

    fun findPageBy(
        pageable: Pageable,
        name: String?,
        email: String?,
        memberId: String?
    ): Page<Member> {
        return memberRepository.findPage(
            pageable = pageable,
            name = name,
            email = email,
            memberId = memberId,
        )
    }

    fun update(members: List<Member>) {
        memberRepository.saveAll(members)
    }
}