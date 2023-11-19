package com.example.restdocssample

import com.example.restdocssample.member.Member
import com.example.restdocssample.member.MemberRepository
import com.example.restdocssample.member.MemberStatus
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@SpringBootApplication
class RestdocsSampleApplication

fun main(args: Array<String>) {
    runApplication<RestdocsSampleApplication>(*args)
}


@Component
class DataSetup(
    private val memberRepository: MemberRepository
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        memberRepository.saveAll(
            listOf(
                Member("yun@bbb.com", "yun", MemberStatus.BAN),
                Member("jin@bbb.com", "jin", MemberStatus.NORMAL),
                Member("han@bbb.com", "han", MemberStatus.NORMAL),
                Member("jo@bbb.com", "jo", MemberStatus.LOCK)
            )
        )
    }
}

//inline inline fun <reified T> RestTemplate.responseResult(): ResponseResult<T>

suspend inline fun <reified T> HttpResponse.responseResult(): ResponseResult<T> {
    return when {
        status.isSuccess() -> ResponseResult.Success(body())
        else -> {
            val responseBody = bodyAsText()
            ResponseResult.Failure(
                when {
                    isServiceErrorResponseSerializeAble(responseBody) -> defaultObjectMapper.readValue(responseBody, ErrorResponse::class.java)
                    else -> defaultErrorResponse
                }
            )
        }
    }
}

/**
 *  표준 [ErrorResponse]를 Serialize 가능 여부
 */
fun isServiceErrorResponseSerializeAble(responseBody: String): Boolean {
    return when (val rootNode = defaultObjectMapper.readTree(responseBody)) {
        null -> false
        else -> rootNode.path("message").isTextual && rootNode.path("status").isNumber && rootNode.path("code").isTextual
    }
}

val defaultObjectMapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .registerModules(JavaTimeModule(), Jdk8Module())
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .apply { this.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE }


val defaultErrorResponse = ErrorResponse(
    code = ErrorCode.INVALID_INPUT_VALUE
)