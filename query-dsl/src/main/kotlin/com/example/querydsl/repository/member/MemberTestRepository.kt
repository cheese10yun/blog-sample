package com.example.querydsl.repository.member

import com.example.querydsl.domain.Member
import com.example.querydsl.repository.support.Querydsl4RepositorySupport
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.function.Function
import com.example.querydsl.domain.QMember.member as qMember

@Repository
class MemberTestRepository : Querydsl4RepositorySupport(Member::class.java) {


    fun simpleSelect(): List<Member> {
        return selectFrom(qMember)
                .fetch()
    }

    fun simplePage(pageable: Pageable): Page<Member> {
        return applyPagination(pageable, Function {
            selectFrom(qMember)
        })
    }
}