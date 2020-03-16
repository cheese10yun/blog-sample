package com.spring.test.member

import com.spring.test.SpringApiTestSupport
import com.spring.test.SpringTestSupport
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post

//@WebMvcTest(MemberApi::class)
//@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)

internal class MemberApiMockTest(
) : SpringApiTestSupport() {

//    @MockBean
//    lateinit var memberService: MemberService
//
//    @MockBean
//    lateinit var memberRepository: MemberRepository

    @Test
    internal fun `create member`() {
        val name = "asd"
        val email = "asd@asd.com"


//        given(memberService.create(name, email)).willReturn(Member(name, email))

        mockMvc.post("/members") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                      "name": "$name",
                      "email": "$email"
                    }
                """.trimIndent()
            }
            .andDo { print() }
            .andExpect {
                status { isOk }
            }
    }
}