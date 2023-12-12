package com.example.mongostudy.member

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
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
        memberStatus: MemberStatus?
    ): Page<Member> {
        return memberRepository.findPageBy(
            pageable = pageable,
            name = name,
            email = email,
            dateJoinedFrom = dateJoinedFrom,
            dateJoinedTo = dateJoinedTo,
            memberStatus = memberStatus
        )
    }

    fun updateBulkTest(pairs: List<Pair<() -> Query, () -> Update>>) {
        memberRepository.updateBulkTest(pairs)
    }

    fun update(members: List<Member>) {
        memberRepository.saveAll(members)
    }
}