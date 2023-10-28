package com.example.restdocssample

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking

class XXClient {

    private val client = HttpClient {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            json()
        }
    }

    fun getSample(id: String): ResponseResult<SampleResponse> {
        return runBlocking {
            client
                .get("/${id}/")
                .responseResult<SampleResponse>()
        }
    }

}

class SampleResponse(
    var name: String,
    var email: String
) {
    init {

        this.name = name.trim()
        this.email = email.trim()
    }
}