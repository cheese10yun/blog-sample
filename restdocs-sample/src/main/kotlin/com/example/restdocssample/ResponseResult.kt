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
 *
 * HTTP 응답을 나타내는 sealed 클래스. 성공 또는 실패의 결과를 포함한다.
 * @param T 응답 본문의 제네릭 타입
 */
sealed class ResponseResult<out T> {

    /**
     * HTTP 2xx 정상 응답을 나타내는 데이터 클래스.
     *
     * @param body 응답 본문.
     */
    data class Success<out T>(val body: T) : ResponseResult<T>()

    /**
     * HTTP 4xx, 5xx 오류 응답을 나타내는 데이터 클래스.
     *
     * @param errorResponse 오류 응답 정보.
     */
    data class Failure(val errorResponse: ErrorResponse) : ResponseResult<Nothing>()

    // 성공 여부를 확인하는 속성.
    val isSuccess: Boolean
        get() = this is Success

    // 실패 여부를 확인하는 속성.
    val isFailure: Boolean
        get() = this is Failure

    /**
     * Success 상태일 때 실행될 콜백 함수.
     *
     * @param action 성공 시 실행할 액션.
     */
    inline fun onSuccess(action: (T) -> Unit): ResponseResult<T> {
        if (this is Success) {
            action(body)
        }
        return this
    }

    /**
     * Failure 상태일 때 실행될 콜백 함수.
     *
     * @param action 실패 시 실행할 액션.
     */
    inline fun onFailure(action: (ErrorResponse) -> Unit): ResponseResult<T> {
        if (this is Failure) {
            action(errorResponse)
        }
        return this
    }

    /**
     * Failure 상태일 경우 null을 반환하며, 그 외의 경우 주어진 액션을 수행한다.
     */
    fun getOrNull(): T? = if (this is Success) body else null


    /**
     * Failure 상태인 경우 주어진 default 값을 반환하며, Success 상태일 경우 주어진 변환 함수를 적용한다.
     *
     * @param default 기본 반환 값.
     * @param transform 변환 함수.
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