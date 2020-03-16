package com.spring.test.member

import com.spring.test.SpringTestSupport
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@AutoConfigureMockMvc
internal class MemberApiTest(
    private val mockMvc: MockMvc
) : SpringTestSupport() {

    @Test
    internal fun `get members`() {

        (1..20).map {
            Member("name-$it", "email-$it@asd.com")
        }.let {
            saveAll(it)
        }

        mockMvc.get("/members") {
                param("page", "0")
                param("size", "5")
                contentType = MediaType.APPLICATION_JSON
            }
            .andDo { print() }

            .andExpect {
                status { isOk }
            }
    }
}