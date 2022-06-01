package com.example.testbandwidth

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class MemberFindServiceMockTestSupport {
    // 외부 인프라
    @InjectMocks
    lateinit var memberFindService: MemberFindService

    @Mock
    lateinit var memberRepository: MemberRepository

    @Test
    fun mock_test() {
        //given
        val member = Member("yun")
        BDDMockito.given(memberRepository.findByName("yun")).willReturn(member)

        //when
        val findMember: Member = memberFindService.findByName("yun")

        //then
        then(findMember.name).isEqualTo("yun")
    }
}