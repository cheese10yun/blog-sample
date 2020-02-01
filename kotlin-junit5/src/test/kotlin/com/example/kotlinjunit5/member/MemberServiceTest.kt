package com.example.kotlinjunit5.member

import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@ExtendWith(MockitoExtension::class)
internal class MemberServiceTest {

    @InjectMocks
    private lateinit var memberService: MemberService

    @Mock
    private lateinit var memberRepository: MemberRepository

    @Test
    internal fun `mock test`() {
        //given
        val name = "new name"
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(Member("something@asd.com", name)))

        //when
        val member = memberService.updateName(name, 1)

        //then
        println(member)

    }

    @Test
    internal fun `member service mock test`() {

        //given
        val name = "new name"
        given(memberRepository.findById(anyLong())).willReturn(Optional.empty())

        //when & then
        thenThrownBy {
            memberService.updateName(name, 1)
        }
            .isExactlyInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("is not existed")
    }

    @Test
    internal fun `given절을 여러번 호출을 할 수 있다`() {
        //given
        val name = "new_name"
        given(memberRepository.findById(anyLong()))
            .willReturn(Optional.empty())
            .willReturn(Optional.of(Member("something@asd.com", name)))

        //when

        thenThrownBy {
            memberService.updateName(name, 1)
        }
            .isExactlyInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("is not existed")


        val member = memberService.updateName(name, 1)

        //then
    }

    @Test
    internal fun `verify test`() {
        //given
        val name = "new name"
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(Member("something@asd.com", name)))

        //when
        val member = memberService.updateName(name, 1)

        //then
        println(member)
        then(memberRepository).should(times((1))).findById(anyLong())
        then(memberRepository).should(times((1))).findByName(name)
        then(memberRepository).should(never()).save(any())
    }
}