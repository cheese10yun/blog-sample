package com.example.jpanplus1.member

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class MemberTest {

    @ParameterizedTest
    @CsvSource(
            "asd@asd.com,asd",
            "sample@sample,sample",
            "oop@asd.com,JAVA"
    )
    @DisplayName("객체 생성테스트")
    internal fun name(email: String, name: String) {
        val member = Member(email = email, name = name)
        assertThat(member.email).isEqualTo(email)
        assertThat(member.name).isEqualTo(name)
    }
}