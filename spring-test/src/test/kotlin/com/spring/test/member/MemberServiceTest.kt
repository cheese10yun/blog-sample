package com.spring.test.member

import com.spring.test.SpringTestSupport
import org.junit.jupiter.api.Test

internal class MemberServiceTest(
    private val memberService: MemberService
) : SpringTestSupport() {

    @Test
    internal fun service() {

        val member = memberService.create("as", "asd")

        println(member)

    }
}