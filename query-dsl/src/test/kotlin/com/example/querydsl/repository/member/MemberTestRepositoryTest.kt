package com.example.querydsl.repository.member

import com.example.querydsl.SpringBootTestSupport
import com.example.querydsl.domain.Member
import com.example.querydsl.domain.Team
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import jakarta.persistence.EntityManager
import com.example.querydsl.domain.QTeam.team as qTeam

@Transactional
internal class MemberTestRepositoryTest(
    private val em: EntityManager,
    private val memberTestRepository: MemberTestRepository
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
    internal fun `JPQL 쿼리 실행시 플러시 자동 호출`() {
        val teamA = Team("teamA")
        val teamB = Team("teamB")

        em.persist(teamA)
        em.persist(teamB)


        val teams = query.select(qTeam)
            .from(qTeam)
            .fetch()

        for (team in teams) {
            println("team : $team")
        }
    }

    @Test
    internal fun `simplePage test`() {
        val members = memberTestRepository.simplePage(PageRequest.of(0, 4))

        for (member in members) {
            println(member)

        }
    }
}