package com.example.querydsl.repository.member

import com.example.querydsl.domain.Member
import com.example.querydsl.repository.support.QuerydslNewRepositorySupport
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import com.example.querydsl.domain.QMember.member as qMember

@Repository
class MemberTestNewRepository : QuerydslNewRepositorySupport(Member::class.java) {


    fun simpleSelect(): List<Member> {
        return selectFrom(qMember)
            .fetch()
    }

    fun simplePage(pageable: Pageable): Page<Member> {
        return applyPagination(
            pageable = pageable,
            contentQuery = { selectFrom(qMember) },
            countQuery = { select(qMember.count()).from(qMember) }
        )
    }
}