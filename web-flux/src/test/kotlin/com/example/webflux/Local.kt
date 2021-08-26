package com.example.webflux

import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

class Local {

    @Test
    fun `WebClient Test`() {
        val bodyToMono = WebClient.create()
            .method(HttpMethod.PATCH)
            .uri("http://localhost")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                  "by_who": "1"
                }
            """.trimIndent()
            )
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }
}