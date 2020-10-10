package com.example.querydsl.domain

import com.example.querydsl.SpringBootTestSupport
import com.example.querydsl.repository.member.MemberRepository
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional

@Transactional
internal class MemberWhereTest(
    private val memberRepository: MemberRepository
) : SpringBootTestSupport() {

    @Test
    internal fun `where sql MemberStatus BAN test`() {
        //given
        val teamA = save(Team("teamA"))
        val memberId = save(Member("name", 10, MemberStatus.BAN, teamA)).id!!

        //when
        val member = memberRepository.findById(memberId)

        //then
        then(member.isPresent).isFalse()
        println("member 조회 여부 : ${member.isPresent}")
    }

    @Test
    internal fun `where sql MemberStatus NORMAL test`() {
        //given
        val teamA = save(Team("teamA"))
        val memberId = save(Member("name", 10, MemberStatus.NORMAL, teamA)).id!!

        //when
        val member = memberRepository.findById(memberId)

        //then
        then(member.isPresent).isTrue()
        println("member 조회 여부 : ${member.isPresent}")
    }
}