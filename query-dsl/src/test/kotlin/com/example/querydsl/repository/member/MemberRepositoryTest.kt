package com.example.querydsl.repository.member

import com.example.querydsl.SpringBootTestSupport
import com.example.querydsl.domain.Member
import com.example.querydsl.domain.Team
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import javax.persistence.EntityManager


internal class MemberRepositoryTest(
        private val em: EntityManager,
        private val memberRepository: MemberRepository
) : SpringBootTestSupport() {

    @BeforeEach
    internal fun setUp() {
        val teamA = Team("teamA")
        val teamB = Team("teamB")

        em.persist(teamA)
        em.persist(teamB)

        val member1 = Member(username = "member1", age = 10, team = teamA)
        val member2 = Member(username = "member2", age = 20, team = teamA)
        val member3 = Member(username = "member3", age = 30, team = teamB)
        val member4 = Member(username = "member4", age = 40, team = teamB)

        em.persist(member1)
        em.persist(member2)
        em.persist(member3)
        em.persist(member4)

        em.flush()
        em.clear()
    }

    @Test
    internal fun `search test`() {
        val member = memberRepository.search("member1", 10)

        then(member.username).isEqualTo("member1")
        then(member.age).isEqualTo(10)
    }

    @Test
    internal fun `search page`() {
        val search = memberRepository.search("member1", 10, PageRequest.of(0, 4))
        println(search)
    }
}