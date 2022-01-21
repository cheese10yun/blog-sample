package com.example.msaerrorresponse

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.ResponseResultOf
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost

class UserClient(
    private val host: String = "http://localhost:8080"
) {

    fun registerUser() {
        val jsonBody = "$host/api/v1/users"
            .httpPost()
            .header(Headers.CONTENT_TYPE, "application/json")
            .jsonBody(
                """
                    {
                      "name": "name test",
                      "email": "asd@test.asd.com"
                    }
                    """.trimIndent()
            )
            .response()
    }
}