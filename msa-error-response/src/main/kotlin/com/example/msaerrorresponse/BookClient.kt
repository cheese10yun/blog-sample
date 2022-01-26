package com.example.msaerrorresponse.`1`

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.ResponseResultOf
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.core.isSuccessful
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.onError

class BookClient(
    private val host: String = "http://localhost:8080"
) {

    fun getUser(bookId: Long) =
        "$host/api/v1/books/$bookId"
            .httpGet()
            .responseObject<Book>()
            .third
            .onError {
                if (it.response.isSuccessful.not()) {
                    throw IllegalArgumentException("bookId: $bookId not found")
                }
            }
            .get()


    fun updateBookStatus(
        status: String
    ) =
        "$host/api/v1/books"
            .httpPost()
            .header(Headers.CONTENT_TYPE, "application/json")
            .jsonBody(
                """
                    {
                      "status": "$status"
                    }
                """.trimIndent()
            )
            .response()
            .third
            .onError {
                if (it.response.isSuccessful.not()) {
                    throw IllegalArgumentException("Book Status Failed")
                }
            }
            .get()
}

data class Book(
    val id: Long,
    val name: String,
    val status: String
)