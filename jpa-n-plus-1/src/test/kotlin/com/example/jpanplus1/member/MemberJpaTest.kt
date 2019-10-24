package com.example.jpanplus1.member

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.jdbc.Sql

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class MemberJpaTest(val memberRepository: MemberRepository) {

    @Test
    @DisplayName("asd")
    internal fun name() {
        val members = memberRepository.findAll()
        assertThat(members).hasSize(0)
        for (member in members) {
            println(member)
        }
    }

    @Test
    @Sql("/test-member-data.sql")
    @DisplayName("SQL TEST")
    internal fun sqlTest() {
        val members = memberRepository.findAll()
//        assertThat(members).hasSize(7)
    }
}