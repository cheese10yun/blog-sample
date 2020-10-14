package com.example.jacoco.domain

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

internal class MemberTest {

    @Test
    internal fun `member 생성 테스트`() {
        //given
        val name = "name"
        val email = "email@ads.com"

        //when
        val member = Member(
            name = name,
            email = email
        )

        //then
        then(member.name).isEqualTo(name)
        then(member.email).isEqualTo(email)
    }
}