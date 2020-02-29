package com.example.querydsl.repository

import com.example.querydsl.SpringBootTestSupport
import com.example.querydsl.domain.Team
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import com.example.querydsl.domain.QTeam.team as qTeam

@Transactional
internal class BulkTest(
    private val em: EntityManager
) : SpringBootTestSupport() {

    @BeforeEach
    internal fun setUp() {
        val teams = listOf(
            Team("team_1"),
            Team("team_2"),
            Team("team_3"),
            Team("team_4"),
            Team("team_5"),
            Team("team_6"),
            Team("team_7"),
            Team("team_8"),
            Team("team_9"),
            Team("team_10")
        )

        for (team in teams) {
            em.persist(team)
        }
    }

    @Test
    internal fun `bulk test`() {
        // team 전체를 조회한다. team name은 team_x 이다.
        val teams = query.selectFrom(qTeam).fetch()

        for (team in teams) {
            println("before update team : $team")
        }

        val ids = teams.map { it.id!! }

        // team  name 전체를 none name으로 변경한다.
        val updateCount = query.update(qTeam)
            .set(qTeam.name, "none name")
            .where(qTeam.id.`in`(ids))
            .execute()

        println("update count : $updateCount")


        for (team in teams) {
            println("after update team : $team")
        }


        // em.clear() // 영속성 컨텍스트를 초기화 한다.

        val newSelectTeams = query.selectFrom(qTeam).fetch()

        for (team in newSelectTeams) {
            println("new select team : $team")
        }
    }
}