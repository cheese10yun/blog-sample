package com.example.mongostudy.member

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.query.Criteria
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

    fun updateTest() {
        val listOf = listOf(
            { Query(Criteria.where("_id").`is`(1)) } to { Update().set("rule_id", 24) },
            { Query(Criteria.where("_id").`is`(4)) } to { Update().set("rule_id", 21) },
            { Query(Criteria.where("_id").`is`(5)) } to { Update().set("rule_id", 21) },
            { Query(Criteria.where("_id").`is`(2)) } to { Update().set("rule_id", 22) }
        )



        memberRepository.updateEmail(listOf)
    }
}