//package com.example.kotlincoroutine
//
//import org.assertj.core.api.BDDAssertions.then
//import org.junit.jupiter.api.Test
//
//class ResponseResultTest {
//
//    @Test
//    fun `getOrElse - Success`() {
//        val result = ResponseResult.Success()
//        val value = result.getOrElse(
//            default = 0,
//            onSuccess = { it + 10 }
//        )
//
//        then(value).isEqualTo(52)
//    }
//
//    @Test
//    fun `getOrElse - Failure`() {
//        val errorResponse = ErrorResponse(500, "Internal Server Error")
//        val result = ResponseResult.Failure(errorResponse)
//        val value = result.getOrElse(
//            default = 0,
//            onSuccess = { 42 }
//        )
//        then(value).isEqualTo(0)
//    }
//
//    @Test
//    fun `flatMap - Success to Success`() {
//        val result = ResponseResult.Success()
//        val transformed = result.flatMap { ResponseResult.Success() }
//
//        then(transformed).isEqualTo(ResponseResult.Success())
//    }
//
//    @Test
//    fun `flatMap - Success to Failure`() {
//        val result = ResponseResult.Success()
//        val errorResponse = ErrorResponse(404, "Not Found")
//        val transformed = result.flatMap { ResponseResult.Failure(errorResponse) }
//
//
//        then(transformed).isEqualTo(ResponseResult.Failure(errorResponse))
//    }
//
//    @Test
//    fun `flatMap - Failure`() {
//        val errorResponse = ErrorResponse(500, "Internal Server Error")
//        val result = ResponseResult.Failure(errorResponse)
//        val transformed = result.flatMap { ResponseResult.Success() }
//        then(transformed).isEqualTo(ResponseResult.Failure(errorResponse))
//    }
//
//    @Test
//    fun `onComplete - Success`() {
//        val result = ResponseResult.Success()
//        var successValue = 0
//        var failureValue = ErrorResponse(0, "")
//
//        result.onComplete(
//            onSuccess = { successValue = it },
//            onFailure = { failureValue = it }
//        )
//
//        then(successValue).isEqualTo(42)
//        then(failureValue).isEqualTo(ErrorResponse(0, ""))
//    }
//
//    @Test
//    fun `onComplete - Failure`() {
//        val errorResponse = ErrorResponse(404, "Not Found")
//        val result = ResponseResult.Failure(errorResponse)
//        var successValue = 0
//        var failureValue = ErrorResponse(0, "")
//
//        result.onComplete(
//            onSuccess = { successValue = it },
//            onFailure = { failureValue = it }
//        )
//
//        then(successValue).isEqualTo(0)
//        then(failureValue).isEqualTo(errorResponse)
//    }
//
//    @Test
//    fun `isSuccess - Success`() {
//        val result = ResponseResult.Success()
//        then(result.isSuccess).isTrue()
//
//        result.body
//    }
//
//    @Test
//    fun `isSuccess - Failure`() {
//        val errorResponse = ErrorResponse(500, "Internal Server Error")
//        val result = ResponseResult.Failure(errorResponse)
//        then(result.isSuccess).isFalse()
//    }
//
//    @Test
//    fun testRecover() {
//        // 실패한 ResponseResult
//        val failureResult: ResponseResult<Int> = ResponseResult.Failure(ErrorResponse(404, "Not Found"))
//
//        // 실패 시 복구 로직을 정의합니다.
//        val recoveredResult = failureResult.recover { errorResponse ->
//            // 실패 시 복구 로직
//            if (errorResponse.status == 404) {
//                ResponseResult.Success() // 404 에러 시 디폴트 값으로 복구
//            } else {
//                ResponseResult.Failure(ErrorResponse(500, "Internal Server Error")) // 기타 에러 시 다른 에러 응답으로 복구
//            }
//        }
//
//        // 복구된 결과를 검증합니다.
//
//        then(recoveredResult is ResponseResult.Success).isTrue()
//
//        then((recoveredResult as ResponseResult.Success).body).isEqualTo(0)
//    }
//}
//
