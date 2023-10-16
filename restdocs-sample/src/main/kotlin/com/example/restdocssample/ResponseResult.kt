package com.example.restdocssample


sealed class HttpResult<out T> {

    data class Success<out T>(val value: T) : HttpResult<T>()
    data class Failure(val error: ErrorResponse) : HttpResult<Nothing>()

    fun isSuccess(): Boolean = this is Success<T>
    fun isFailure(): Boolean = this is Failure

    fun getOrNull(): T? = when (this) {
        is Success -> value
        else -> null
    }

    fun exceptionOrNull(): ErrorResponse? = when (this) {
        is Failure -> error
        else -> null
    }

    inline fun <R> map(transform: (T) -> R): HttpResult<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    inline fun recover(transform: (ErrorResponse) -> @UnsafeVariance T): T = when (this) {
        is Success -> value
        is Failure -> transform(error)
    }

    // 여기에 추가로 유용하다고 생각하는 함수들을 구현할 수 있습니다.
}