package com.example.jpanplus1.member

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
internal class MemberJpaTest(val memberRepository: MemberRepository) {

    @Test
    @DisplayName("asd")
    internal fun name() {
        val members = memberRepository.findAll()
        assertThat(members).hasSize(15)
    }

    @Test
    @Sql("/member-data.sql")
    @DisplayName("SQL TEST")
    internal fun sqlTest() {
        val members = memberRepository.findAll()
        assertThat(members).hasSizeGreaterThan(7)
    }
}