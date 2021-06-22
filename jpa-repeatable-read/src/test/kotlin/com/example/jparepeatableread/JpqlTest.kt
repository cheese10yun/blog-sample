package com.example.jparepeatableread

import javax.persistence.EntityManager
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
class JpqlTest(
    private val em: EntityManager,
    private val teamRepository: TeamRepository
) {

    @Test
    fun `JPQL 조회 테스트`() {
        //given
        // insert into team (id, name) values (null, ?)
        val teamA = Team(name = "teamA")
        em.persist(teamA) // teamA 저장

        // insert into member (id, age, team_id, username) values (null, ?, ?, ?)
        val member1 = Member(username = "member1", age = 10, team = teamA) // member1에 teamA 연결해서 저장
        // insert into member (id, age, team_id, username) values (null, ?, ?, ?)
        val member2 = Member(username = "member2", age = 20, team = teamA) // member2에 teamA 연결해서 저장
        em.persist(member1)
        em.persist(member2)

        //when
        // select team0_.id as id1_1_0_, members1_.id as id1_0_1_, team0_.name as name2_1_0_, members1_.age as age2_0_1_, members1_.team_id as team_id4_0_1_, members1_.username as username3_0_1_, members1_.team_id as team_id4_0_0__, members1_.id as id1_0_0__ from team team0_ inner join member members1_ on team0_.id=members1_.team_id where team0_.name=?
        em.clear()
        val team = teamRepository.findFetchJoinBy("teamA")

        //then
        then(team.members).hasSize(2)
    }
}