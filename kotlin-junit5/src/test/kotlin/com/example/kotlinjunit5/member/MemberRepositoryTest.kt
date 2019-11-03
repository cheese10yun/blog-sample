package com.example.kotlinjunit5.member

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import java.time.LocalDateTime
import javax.persistence.EntityManager


@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles("test")
@DataJpaTest
internal class MemberRepositoryTest(
        val memberRepository: MemberRepository,
        val entityManager: EntityManager

) {

    @Test
    internal fun `member save test`() {
        //given
        val email = "asd@asd.com"
        val name = "name"

        //when
        val member = memberRepository.save(Member(email, name))

        //then

        // 기존 사용법 assertThat
        assertThat(member.email).isEqualTo(email)
        assertThat(member.name).isEqualTo(name)
        assertThat(member.createdAt).isBeforeOrEqualTo(LocalDateTime.now())
        assertThat(member.updatedAt).isBeforeOrEqualTo(LocalDateTime.now())

        // BDD 사용법
        then(member.email).isEqualTo(email)
        then(member.name).isEqualTo(name)
        then(member.createdAt).isBeforeOrEqualTo(LocalDateTime.now())
        then(member.updatedAt).isBeforeOrEqualTo(LocalDateTime.now())
    }


    @Test
    internal fun `find test`() {
        //given
        memberRepository.saveAll(listOf(
                Member("email1@asd.com", "kim"),
                Member("email2@asd.com", "kim"),
                Member("email3@asd.com", "kim"),
                Member("email4@asd.com", "name"),
                Member("email5@asd.com", "name")
        ))

        //when
        val members = memberRepository.findByName("kim")

        //then
        then(members).anySatisfy {
            then(it.name).isEqualTo("kim")
        }
    }

    @Test
    internal fun `문장 검사`() {
        then("AssertJ is best matcher").isNotNull()
                .startsWith("AssertJ")
                .contains(" ")
                .endsWith("matcher")
    }
}