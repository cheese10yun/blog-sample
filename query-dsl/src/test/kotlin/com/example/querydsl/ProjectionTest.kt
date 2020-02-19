package com.example.querydsl

import com.example.querydsl.domain.Member
import com.example.querydsl.domain.Team
import com.example.querydsl.dto.QMemberDto
import com.querydsl.jpa.impl.JPAQueryFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.persistence.EntityManager
import com.example.querydsl.domain.QMember.member as qMember

class ProjectionTest(
    private val em: EntityManager
) : SpringBootTestSupport() {

    val query = JPAQueryFactory(em)

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
    internal fun `projection annotation`() {
        val members = query
            .select(QMemberDto(
                qMember.username,
                qMember.age.max()
            ))
            .from(qMember)
            .fetch()

        for (member in members) {
            println(member)
        }
    }
}