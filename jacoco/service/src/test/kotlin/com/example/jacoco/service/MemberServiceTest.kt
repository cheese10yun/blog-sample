package com.example.jacoco.service

import com.example.jacoco.domain.Member
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

internal class MemberServiceTest {

    @Test
    internal fun `member test`() {
        //given
        val member = Member(name = "name", email = "emad@asd.com")
        val memberService = MemberService()

        //when
        val findMember = memberService.findByName("name")

        //then
        then(member.name).isEqualTo(findMember.name)
    }
}