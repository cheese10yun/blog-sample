package com.example.querydsl.domain

import com.example.querydsl.SpringBootTestSupport
import com.example.querydsl.dto.MemberDtoQueryProjection
import com.querydsl.core.types.Projections
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import com.example.querydsl.domain.QMember.member as qMember

@Transactional
internal class MemberTest2(
    private val em: EntityManager
) : SpringBootTestSupport() {


    @Test
    internal fun `query dsl projection dto 2`() {

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

        Thread.sleep(100_000)

        val privilegedMode = mysqlTestContainer.isPrivilegedMode
        println("=======================")
        println(privilegedMode)
        println("=======================")

        val members = query
            .select(Projections.constructor(
                MemberDtoQueryProjection::class.java,
                qMember.username,
                qMember.age.max().`as`("age")
            ))
            .from(qMember)
            .groupBy(qMember.age)
            .fetch()

        for (member in members) {
            println(member)
        }
    }
}