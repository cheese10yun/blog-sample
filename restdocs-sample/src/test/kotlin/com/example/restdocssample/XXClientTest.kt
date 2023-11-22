package com.example.restdocssample

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class XXClientTest {

    val XXClient = XXClient()

    @Test
    fun `getIntegrationInfo`() {
        XXClient.getSample("id")
            .onFailure { errorResponse -> }
            .getOrThrow { it }
    }

    @Test
    fun notull보장() {
        // null 보장 니가 null 처리
        val orNull = XXClient.getSample("id")
            .getOrNull { it }

        // null 보장, 오류 발생시 error callback
        val nullOrCallback: SampleResponse = XXClient.getSample("id")
            .onFailure { errorResponse ->
                // 오류 콜백 처리
//                if (errorResponse.status.isServerError()) {
//                    throw RuntimeException()
//                }
                // 로그 찍고 그만
            }
            .getOrThrow { it }


        // getOrThrow notnull 보장
        val notnull = XXClient.getSample("id")
            .getOrThrow { it }

        // 실패나면 default 라도 줘
        val default = XXClient.getSample("id")
            .getOrDefault(
                default = SampleResponse(
                    name = "default value",
                    email = "default value"
                ),
                transform = { it }
            )

        val default11 = XXClient.getSample("id")
            .recover { errorResponse ->
                when (errorResponse.status.isClientError()) {
                    true -> ResponseResult.Success(default)
                    else -> ResponseResult.Failure(errorResponse)
                }
            }
            .getOrThrow { it }


    }
}