package com.example.querydsl

import com.example.querydsl.domain.Member
import com.example.querydsl.domain.Team
import com.querydsl.jpa.impl.JPAQueryFactory
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import javax.persistence.EntityManager
import com.example.querydsl.domain.QMember.member as qMember
import com.example.querydsl.domain.QTeam.team as qTeam


class PersistenceContextTest(
        private val em: EntityManager
) : SpringBootTestSupport() {

    val query = JPAQueryFactory(em)

    @Test
    internal fun `persistence context test`() {
        //given
        val teamA = Team("teamA")
        em.persist(teamA)

        val member1 = Member(username = "member1", age = 10, team = teamA)
        val member2 = Member(username = "member2", age = 20, team = teamA)
        em.persist(member1)
        em.persist(member2)

//        teamA.members.add(member1)
//        teamA.members.add(member2)

        em.flush()
        em.clear()

        //when
        val team = query
                .selectFrom(qTeam)
                .join(qTeam.members, qMember).fetchJoin()
                .where(qTeam.name.eq("teamA"))
                .fetchOne()!!

        //then
        val members = team.members
        then(members).anySatisfy {
            then(it.username).isIn("member1", "member2")
        }
    }
}