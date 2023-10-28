package com.example.restdocssample


/**
 * # 설명
 * HTTP 통신이 이후 Response 객체 처리 및 실패처리에 대한 서포트 객체
 *
 * ## 에졔
 *
 * ### 2xx 아닌 경우 예외 발생, ResponseBody Notnull 보장 케이스
 * ~~~kotlin
 * val response = result
 *    .onFailure { errorResponse -> getOrThrow(errorResponse) }
 *    .getOrThrow { responseBody -> responseBody }
 * ~~~
 * response 객체가 notnull을 보장, 2xx가 아닌 경우 예외 발생
 *
 * ### 더 구체적인 예외를 발생 시키고 싶은 케이스
 * ~~~kotlin
 * val response = result
 *    .onFailure { errorResponse -> throw xxxxException(...) }
 *    .getOrThrow { responseBody -> responseBody }
 * ~~~
 * onFailure 에서 예외를 발생 시킨 경우 getOrThrow 으로 T 객체 notnull 보장
 *
 * ### 예외 핸들링 하고 싶지 않은 경우
 * ~~~kotlin
 * val response: T? = result
 *    .onFailure { errorResponse -> ... }
 *    .gerOrNull { responseBody -> responseBody }
 * ~~~
 * onFailure 에서 직접 적인 예외 발생 시키지 않음, errorResponse에 대한 핸들링도 하지 않을 거면 onFailure 생략 가능 하며,
 * 2xx가 아닌 경우 Response 객체가 null, 2xx 경우 T 객체 응답
 *
 * ### 2xx 응답이 아닌 경우 Default 객체 지정하는 케이스
 * ~~~kotlin
 * val response = result
 *    .getOrDefault(
 *          default = Bar(name = "DEFAULT_VALUE"),
 *          transform = { Bar(name = it.name) },
 *      )
 * ~~~
 * 2xx 경우 transform 기반으로 변환, 2xx 아니 경우 default 기반으로 변환
 */
sealed class ResponseResult<out T> {

    data class Success<out T>(val body: T) : ResponseResult<T>()
    data class Failure(val errorResponse: ErrorResponse) : ResponseResult<Nothing>()

    val isSuccess: Boolean
        get() = this is Success

    val isFailure: Boolean
        get() = this is Failure

    /**
     * [Success] 경우 콜백
     */
    inline fun onSuccess(action: (T) -> Unit): ResponseResult<T> {
        if (this is Success) {
            action(body)
        }
        return this
    }

    /**
     * api a -> api b -> api c ->
     * c -> b - a
     * ErrorResponse ->
     * [Failure] 경우 콜백, 사용하는 곳에서 ErrorResponse에 따라 예외를 발생 여부를 진행
     */
    inline fun onFailure(action: (ErrorResponse) -> Unit): ResponseResult<T> {
        if (this is Failure) {
            action(errorResponse)
        }
        return this
    }

    /**
     * [Failure] 경우에는 null이 응답 된다.
     */
    inline fun getOrNull(action: (T) -> @UnsafeVariance T): T? {
        return when (this) {
            is Success -> action(body)
            else -> null
        }
    }

    /**
     * [Failure] 경우에는 예외가 발행하기 때문에 Notnull을 보장한다.
     */
    inline fun getOrThrow(action: (T) -> @UnsafeVariance T): T {
        when (this) {
            is Success -> return action(body)
            is Failure -> {
                when {
                    errorResponse.status.isClientError() -> throw ServiceException(errorResponse = errorResponse, code = ErrorCode.INVALID_INPUT_VALUE )
                    else -> throw ServiceException(errorResponse = errorResponse, code = ErrorCode.SERVER_ERROR)
                }
            }
        }
    }

    /**
     * [Failure] 상태인 경우 [default] 기반으로 반환하고, [Success] 경우 반환 진행
     */
    inline fun <R> getOrDefault(default: R, transform: (T) -> R): R {
        return when (this) {
            is Success -> transform(body)
            is Failure -> default
        }
    }

    /**
     * [Success] 상태인 경우 T -> R 변환
     */
    inline fun <R> map(transform: (T) -> R): ResponseResult<R> {
        return when (this) {
            is Success -> Success(transform(body))
            is Failure -> this
        }
    }

    /**
     * [Success] 상태인 경우, ResponseResult<T> -> ResponseResult<R> 변환
     */
    inline fun <R> flatMap(transform: (T) -> ResponseResult<R>): ResponseResult<R> {
        return when (this) {
            is Success -> transform(body)
            is Failure -> this
        }
    }

    /**
     * [Failure] 상태 경우 재시도를 진행하는 경우 사용
     */
    inline fun recover(action: (ErrorResponse) -> ResponseResult<@UnsafeVariance T>): ResponseResult<T> {
        return when (this) {
            is Failure -> action(errorResponse)
            else -> this
        }
    }

}