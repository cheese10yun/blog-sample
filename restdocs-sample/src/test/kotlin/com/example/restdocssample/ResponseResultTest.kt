package com.example.restdocssample

import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ResponseResultTest {

    @Test
    fun `onFailure + getOrThrow 성공 케이스 notnull을 보장 가능`() {
        // given
        val fooName = "name"

        // when
        val result: ResponseResult<Foo> = ResponseResult.Success(Foo(fooName))


        // 4xx -> 5xx
        val response = result
            .onFailure { errorResponse -> {} }
            .getOrThrow()

        // then
        then(response.name).isEqualTo(fooName)
    }

//    @Test
//    fun `onFailure + getOrThrow 실패 케이스 ServiceApiException 발생`() {
//        // given
//        val error = defaultErrorResponse
//
//        // when & then
//        val result: ResponseResult<Foo> = ResponseResult.Failure(error)
//
//        thenThrownBy {
//            result
//                .onFailure { errorResponse -> {} }
//                .getOrThrow()
//        }.isInstanceOf(ServiceException::class.java)
//    }

    @Test
    fun `onFailure + gerOrNull 성공 케이스 T 객체 보장`() {
        // given
        val fooName = "name"

        // when
        val result: ResponseResult<Foo> = ResponseResult.Success(Foo(fooName))
        val response = result
            .onFailure { }
            .getOrNull()

        // then
        then(response).isNotNull
        then(response!!.name).isEqualTo(fooName)
    }

    @Test
    fun `onFailure + gerOrNull 실패 케이스 T 객체 null`() {
        // given
        val error = defaultErrorResponse

        // when & then
        val result: ResponseResult<Foo> = ResponseResult.Failure(error)
        val response = result
            .onFailure { }
            .getOrNull()

        // then
        then(response).isNull()
    }

//    @Test
//    fun `gerOrNull + map 성공 케이스 컨버팅 가능`() {
//        // given
//        val fooName = "name"
//
//        // when
//        val result: ResponseResult<Foo> = ResponseResult.Success(Foo(fooName))
//        val response = result
//            .map { it.name }
//            .getOrNull()
//
//        // then
//        then(response).isNotNull
//        then(response).isEqualTo(fooName)
//    }
//
//    @Test
//    fun `gerOrNull + map 실패 케이스 null 응답`() {
//        // given
//        val error = defaultErrorResponse
//
//        // when
//        val result: ResponseResult<Foo> = ResponseResult.Failure(error)
//        val response = result
//            .map { it.name }
//            .getOrNull()
//
//        // then
//        then(response).isNull()
//    }

    @Test
    fun `gerOrNull + getOrDefault 성공 케이스 T 기반으로 변환`() {
        // given
        val fooName = "name"

        // when
        val result: ResponseResult<Foo> = ResponseResult.Success(Foo(fooName))
        val response = result
            .getOrDefault(
                default = Bar(name = "DEFAULT_VALUE"),
                transform = { Bar(name = it.name) }
            )

        Result

        // then
        then(response).isNotNull
        then(response.name).isEqualTo(fooName)
    }
}

data class Foo(
    val name: String
)

data class Bar(
    val name: String
)