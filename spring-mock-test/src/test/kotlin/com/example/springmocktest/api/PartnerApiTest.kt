package com.example.springmocktest.api

import com.example.springmocktest.SpringApiTestSupport
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post

internal class PartnerApiTest : SpringApiTestSupport() {

    @Test
    internal fun `파트너 등록`() {

        mockMvc.post("/partners") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "name" : "aaaa",
                  "accountHolder" : "aaaa",
                  "accountNumber" :  "aaaa"
                }
            """.trimIndent()
        }.andExpect {
            status { isOk }
        }

    }
}