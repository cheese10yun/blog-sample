package com.example.kotlinjunit5.member

import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.Test

internal class MemberTest {
    @Test
    internal fun `member 생성 테스트`() {
        val member = Member("asd@asd.com", "yun")
        member.validateBeforeSave()
    }

    @Test
    internal fun `member 실패`() {
        val member = Member("", "")
        thenThrownBy { member.validateBeforeSave() }
                .isExactlyInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("empty")
    }
}