package com.example.querydsl.repository.member

import com.example.querydsl.domain.Member
import com.example.querydsl.domain.QMember
import com.example.querydsl.dto.MemberDtoQueryProjection
import com.example.querydsl.dto.QMemberDtoQueryProjection
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.support.PageableExecutionUtils

interface MemberRepository : JpaRepository<Member, Long>, MemberRepositoryCustom

interface MemberRepositoryCustom {

    fun search(username: String?, age: Int?): Member
    fun search(username: String?, age: Int?, page: Pageable): Page<MemberDtoQueryProjection>

}

class MemberRepositoryImpl(
    private val query: JPAQueryFactory
) : MemberRepositoryCustom {

    override fun search(username: String?, age: Int?): Member {
        return query
            .selectFrom(QMember.member)
            .where(searchCondition(username, age))
            .fetchOne()!!

    }

    override fun search(username: String?, age: Int?, page: Pageable): Page<MemberDtoQueryProjection> {
        val content = query
            .select(
                QMemberDtoQueryProjection(
                QMember.member.username,
                QMember.member.age)
            )
            .from(QMember.member)
            .where(searchCondition(username, age))
            .offset(page.offset)
            .limit(page.pageSize.toLong())
            .orderBy()
            .fetch()

        val countQuery = query
            .select(
                QMemberDtoQueryProjection(
                QMember.member.username,
                QMember.member.age)
            )
            .from(QMember.member)
            .where(searchCondition(username, age))

        return PageableExecutionUtils.getPage(content, page) { countQuery.fetchCount() }
    }

    private fun searchCondition(username: String?, age: Int?): BooleanBuilder {
        val booleanBuilder = BooleanBuilder()

        username.let {
            booleanBuilder.and(QMember.member.username.eq(username))
        }

        age.let {
            booleanBuilder.and(QMember.member.age.eq(age))
        }

        return booleanBuilder
    }
}
