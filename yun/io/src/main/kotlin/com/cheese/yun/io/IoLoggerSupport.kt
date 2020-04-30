package com.cheese.yun.io

import com.cheese.yun.support.logger.logger
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets

fun <T : Any> ResponseResultOf<T>.log() {
    val (request, response, result) = this

    request.log()

    when (result) {
        is Result.Failure ->
            if (result.error.response.statusCode > 0) response.log()
            else throw result.error.exception
        is Result.Success -> response.log()
    }
}

private fun Request.log(): Unit {
    val log = LoggerFactory.getLogger(this.javaClass)
    log.info(
        """
                서버에 다음과 같이 요청했습니다.
                ⊙ $method $url
                ├─ Headers: ${headers.map { it.key to it.value.joinToString(", ", "[", "]") }
            .joinToString(", ", "{", "}") { "${it.first}: ${it.second}" }}
                ├─ Parameters: ${parameters.map { it.first to it.second.toString() }}
                └─ Body: ${if (body.isEmpty()) null else kotlin.text.String(body.toByteArray(), StandardCharsets.UTF_8)}
            """.trimIndent()
    )
}

private fun Response.log() {
    val log = LoggerFactory.getLogger(this.javaClass)
    val messageHeader = """
            요청에 대해 서버가 다음과 같이 응답했습니다.
            ⊙ $statusCode $url
        """.trimIndent()

    val messageFooter = """

            └─ Headers: ${headers.map { it.key to it.value.joinToString(", ", "[", "]") }
        .joinToString(", ", "{", "}") { "${it.first}: ${it.second}" }}
        """.trimIndent()

    if (this.isSuccessful || this.isStatusInformational || this.isStatusRedirection) {
        log.info(messageHeader + messageFooter)
    } else {
        val message = messageHeader +
            """

                    ├─ Body: ${if (body().isEmpty()) null else String(body().toByteArray(), StandardCharsets.UTF_8)}
                """.trimIndent() +
            messageFooter

        log.error(message)
    }
}