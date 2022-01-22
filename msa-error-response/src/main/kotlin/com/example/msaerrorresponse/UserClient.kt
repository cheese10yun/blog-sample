package com.example.msaerrorresponse

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.core.isSuccessful
import com.github.kittinunf.fuel.httpPost
import org.springframework.stereotype.Service


@Service
class UserRegistrationService(
    private val objectMapper: ObjectMapper
) {
    fun register(dto: UserRegistrationRequest): Boolean {
        val response = UserClient()
            .postUser(dto.name, dto.email)
            .run {
                Pair(
                    second.isSuccessful,
                    when {
                        second.isSuccessful -> null
                        else -> {
                            objectMapper.readValue(
                                second.body().toByteArray(),
                                ErrorResponse::class.java
                            )
                        }
                    }
                )
            }
        return response.first
    }
}

class UserClient(
    private val host: String = "http://localhost:8080"
) {

    fun postUser(name: String, email: String) =
        "$host/api/v1/users"
            .httpPost()
            .timeout(60000)
            .timeoutRead(60000)
            .header(Headers.CONTENT_TYPE, "application/json")
            .jsonBody(
                """
                    {
                      "name": "$name",
                      "email": "$email"
                    }
                    """.trimIndent()
            )
            .response()
}