package com.cheese.yun.api

import com.cheese.yun.test.support.SpringWebTestSupport
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post

internal class OrderApiTest : SpringWebTestSupport() {

    @Test
    internal fun name() {
        mockMvc.post("/api/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "address": "asdasd",
                    "price": 123.00
                }
                
            """.trimIndent()
        }
    }
}