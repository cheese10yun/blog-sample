package com.example.msaerrorresponse

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.ResponseResultOf
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.core.isSuccessful
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.jackson.responseObject
import org.slf4j.LoggerFactory
import org.springframework.cloud.sleuth.Tracer
import org.springframework.stereotype.Service


@Service
class UserRegistrationService(
    private val objectMapper: ObjectMapper,
    private val userClient: UserClient
) {


//    private val host: String = "http://localhost:8080"
//    fun register(dto: UserRegistrationRequest) {
//        val response = userClient
//            .postUser(dto.name, dto.email)
//            .run {
//                Pair(
//                    second.isSuccessful,
//                    when {
//                        second.isSuccessful -> null
//                        else -> {
//                            objectMapper.readValue(
//                                String(second.body().toByteArray()),
//                                ErrorResponse::class.java
//                            )
//                        }
//                    }
//                )
//            }
//
//        if (response.first.not()) {
//            throw ApiException(response.second!!)
//        }
//    }


}

data class User(
    val id: Long,
    val name: String
)

@Service
class UserClient(
    private val host: String = "http://localhost:8181",
    private val tracer: Tracer
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun getUser(userId: Long): User {

        val currentSpan = tracer.currentSpan()
        val nextSpan = tracer.nextSpan()
        val span = currentSpan ?: nextSpan
        val context = span.context()

        log.info("=======test======")
        log.error("current traceId: ${currentSpan?.context()?.traceId()}")
        log.error("current spanId: ${currentSpan?.context()?.spanId()}")
        log.error("current parentId: ${currentSpan?.context()?.parentId()}")
        log.error("current sampled: ${currentSpan?.context()?.sampled()}")

        log.error("next traceId: ${nextSpan.context().traceId()}")
        log.error("next spanId: ${nextSpan.context().spanId()}")
        log.error("next parentId: ${nextSpan.context().parentId()}")
        log.error("next sampled: ${nextSpan.context().sampled()}")
        log.info("=======test======")


        return "$host/api/v1/users/$userId"
                .httpGet()
                .header("Content-Type", "application/json")
                .header("x-b3-traceid", context.traceId())
                .header("x-b3-spanid", nextSpan.context().spanId())
                .header("x-b3-parentspanid", context.parentId()?: "null")
                .responseObject<User>()
                .third.get()
    }


    fun postUser(name: String, email: String): ResponseResultOf<ByteArray> =
        "$host/b-service"
            .httpPost()
            .header("Content-Type", "application/json")
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