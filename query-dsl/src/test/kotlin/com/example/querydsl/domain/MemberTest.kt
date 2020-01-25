package com.example.querydsl.domain

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
internal class MemberTest(
        private val em: EntityManager
) {

    @Test
    internal fun `member save test`() {

        val teamA = Team("teamA")
        val teamB = Team("teamB")

        em.persist(teamA)
        em.persist(teamB)

        val member1 = Member(username = "1", age = 10, team = teamA)
        val member2 = Member(username = "2", age = 20, team = teamA)
        val member3 = Member(username = "3", age = 30, team = teamB)
        val member4 = Member(username = "4", age = 40, team = teamB)

        em.persist(member1)
        em.persist(member2)
        em.persist(member3)
        em.persist(member4)

        val members = em.createQuery("select m from Member m", Member::class.java).resultList

        for (member in members) {
            println("member -->> : $member")
        }
    }
}