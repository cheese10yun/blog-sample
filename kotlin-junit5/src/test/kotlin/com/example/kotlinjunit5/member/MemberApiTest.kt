package com.example.kotlinjunit5.member

import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles("test")
@AutoConfigureMockMvc
internal class MemberApiTest(
        val memberRepository: MemberRepository,
        val mockMvc: MockMvc
) {

    @Test
    internal fun `members 조회 테스트`() {
        memberRepository.saveAll(listOf(
                Member("email1@asd.com", "jin"),
                Member("email2@asd.com", "yun"),
                Member("email3@asd.com", "wan"),
                Member("email4@asd.com", "kong"),
                Member("email5@asd.com", "joo")
        ))

        mockMvc.get("/members") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$[0].name") { value("jin") }
            jsonPath("$[1].name") { value("yun") }
            jsonPath("$[2].name") { value("wan") }
            jsonPath("$[3].name") { value("kong") }
            jsonPath("$[4].name") { value("joo") }
        }.andDo {
            print()
        }
    }
}

//given(memberRepository.findAll()).willReturn(listOf(
//                Member("asd1@asd.com", "name1"),
//                Member("asd2@asd.com", "name2"),
//                Member("asd3@asd.com", "name3"),
//                Member("asd4@asd.com", "name4")
//        ))