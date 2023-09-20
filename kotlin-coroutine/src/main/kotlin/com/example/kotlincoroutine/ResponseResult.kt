package com.example.kotlincoroutine


data class ErrorResponse(
    val status: Int,
    val message: String
)

sealed class ResponseResult<out T> {

    data class Success<out T>(val body: T) : ResponseResult<T>()
    data class Failure(val errorResponse: ErrorResponse) : ResponseResult<Nothing>()

    val isSuccess: Boolean
        get() = this is Success

    inline fun onFailure(action: (ErrorResponse) -> Unit): ResponseResult<T> {
        if (this is Failure) {
            action(errorResponse)
        }
        return this
    }


    inline fun onSuccess(action: (T) -> @UnsafeVariance T): T? {
        when (this) {
            is Success -> return action(body)
            else -> return null
        }
    }


    /**
     * getOrElse: 실패한 경우에 기본값을 반환하고, 성공한 경우에는 결과를 반환하는 함수입니다.
     */
    inline fun <R> getOrElse(default: R, onSuccess: (T) -> R): R {
        return when (this) {
            is Success -> onSuccess(body)
            is Failure -> default
        }
    }


    /**
     * Success인 경우에만 데이터를 컨버팅하고, Failure 경우 default 값을 리턴한다.
     */
    inline fun <R> map(converter: (T) -> R, default: R): R {
        return when (this) {
            is Success -> converter(body)
            is Failure -> default
        }
    }

    /**
     * flatMap: map 함수와 유사하지만, 함수의 결과가 ResponseResult인 경우에 활용할 수 있습니
     */
    inline fun <R> flatMap(transform: (T) -> ResponseResult<R>): ResponseResult<R> {
        return when (this) {
            is Success -> transform(body)
            is Failure -> this
        }
    }

    /**
     * onComplete: onSuccess와 onFailure를 한 번에 처리하는 함수입니다.
     */
    inline fun onComplete(onSuccess: (T) -> Unit, onFailure: (ErrorResponse) -> Unit) {
        when (this) {
            is Success -> onSuccess(body)
            is Failure -> onFailure(errorResponse)
        }
    }

    inline fun recover(action: (ErrorResponse) -> ResponseResult<@UnsafeVariance T>): ResponseResult<T> {
        return when (this) {
            is Failure -> action(errorResponse)
            else -> this
        }
    }
}