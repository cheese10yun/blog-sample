package com.example.querydsl.domain

import com.example.querydsl.SpringBootTestSupport
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Transactional
internal class TeamWhereTest : SpringBootTestSupport() {

    @Test
    internal fun `where sql test`() {
        //given
        val qTeam = QTeam.team
        val teamA = save(Team("teamA"))
        save(Member("name", 10, MemberStatus.NORMAL, teamA))

        //when
        val findTeam = Optional.of(query.selectFrom(qTeam).where(qTeam.id.eq(teamA.id)).fetchOne()!!)
        //then

        val memberSize = findTeam.get().members.size
        println("member size : $memberSize")
        println("team 조회 여부 : ${findTeam.isPresent}")
    }
}