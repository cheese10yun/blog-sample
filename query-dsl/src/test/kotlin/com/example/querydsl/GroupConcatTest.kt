package com.example.querydsl

import com.example.querydsl.domain.Member
import com.example.querydsl.domain.Team
import com.example.querydsl.dto.QMemberGroupConcat
import com.querydsl.core.types.dsl.Expressions
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import com.example.querydsl.domain.QMember.member as qMember

@ActiveProfiles("mysql")
@Transactional
internal class GroupConcatTest(
    private val em: EntityManager
) : SpringBootTestSupport() {

    @Test
    internal fun `group concat test`() {
        //given
        val teamA = Team("teamA")
        em.persist(teamA)

        (1..20).map {
            em.persist(Member(username = "member-$it", age = it, team = teamA))
        }

        //when
        val members =
            query
                .select(QMemberGroupConcat(
                    Expressions.stringTemplate("group_concat({0})", qMember.username),
                    Expressions.stringTemplate("group_concat({0})", qMember.age)
                ))
                .from(qMember)
                .groupBy(qMember.team)
                .fetch()

        for (member in members) {
            println(member)
        }
    }

    @Test
    internal fun `group concat max length size`() {
        //given
        val teamA = Team("teamA")
        em.persist(teamA)

        (1..1000).map {
            em.persist(Member(username = "member-$it", age = it, team = teamA))
        }

        //when
        val members =
            query
                .select(QMemberGroupConcat(
                    Expressions.stringTemplate("group_concat({0})", qMember.username),
                    Expressions.stringTemplate("group_concat({0})", qMember.age)
                ))
                .from(qMember)
                .groupBy(qMember.team)
                .fetch()

        for (member in members) {
            println(member)
        }
    }
}