package com.example.restdocssample

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.lang.IllegalStateException
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)!!

    /**
     * javax.validation.Valid or @Validated 으로 binding error 발생시 발생한다.
     * HttpMessageConverter 에서 등록한 HttpMessageConverter binding 못할경우 발생
     * 주로 @RequestBody, @RequestPart 어노테이션에서 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        log.error(e.message, e)
        val response = ErrorResponse(ErrorCode.INVALID_INPUT_VALUE, e.bindingResult)
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    /**
     * @ModelAttribut 으로 binding error 발생시 BindException 발생한다.
     * ref https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-modelattrib-method-args
     */
    @ExceptionHandler(BindException::class)
    protected fun handleBindException(e: BindException): ResponseEntity<ErrorResponse> {
        log.error(e.message, e)
        val response = ErrorResponse(ErrorCode.INVALID_INPUT_VALUE, e.bindingResult)
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    /**
     * enum type 일치하지 않아 binding 못할 경우 발생
     * 주로 @RequestParam enum으로 binding 못했을 경우 발생
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    protected fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        log.error(e.message, e)
        val response = ErrorResponse(ErrorCode.METHOD_NOT_ALLOWED)
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    /**
     * 지원하지 않은 HTTP method 호출 할 경우 발생
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    protected fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> {
        log.error(e.message, e)
        val response = ErrorResponse(ErrorCode.METHOD_NOT_ALLOWED)
        return ResponseEntity(response, HttpStatus.METHOD_NOT_ALLOWED)
    }

    @ExceptionHandler(
        value = [
            IllegalArgumentException::class,
            IllegalStateException::class
        ]
    )
    protected fun handleInnerException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error(e.message, e)
        val response = ErrorResponse(ErrorCode.INVALID_INPUT_VALUE)
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    protected fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error(e.message, e)
        val response = ErrorResponse(ErrorCode.INVALID_INPUT_VALUE)
        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}


class ErrorResponse private constructor(
    val message: String,
    val status: Int,
    val code: String,
    val errors: MutableList<FieldError> = mutableListOf(),
    val timestamp: LocalDateTime = LocalDateTime.now()
) {


    constructor(code: ErrorCode) : this(code.message, code.status, code.code)

    constructor(code: ErrorCode, ex: Exception) : this(
        message = when (ex.message) {
            null -> code.message
            else -> ex.message!!
        },
        status = code.status,
        code = code.code
    )

    /**
     * [ErrorResponse.message]를 별도의 message로 표시, 주로 어드민 화면등 비개발자가 보는 메시지로 사용
     */
    constructor(code: ErrorCode, message: String) : this(
        message = message,
        status = code.status,
        code = code.code
    )

    constructor(code: ErrorCode, bindingResult: BindingResult) : this(code) {
        FieldError.addFieldErrors(bindingResult, errors)
    }

    constructor(code: ErrorCode, e: MethodArgumentTypeMismatchException) : this(code) {
        FieldError.addFieldErrors(e, errors)
    }
}

class FieldError(
    val field: String,
    val value: String,
    val reason: String
) {
    companion object {
        fun addFieldErrors(e: MethodArgumentTypeMismatchException, errors: MutableList<FieldError>) {
            errors.add(
                FieldError(
                    field = e.name,
                    value = if (e.value == null) "" else e.value as String,
                    reason = e.errorCode
                )
            )
        }

        fun addFieldErrors(bindingResult: BindingResult, errors: MutableList<FieldError>) {
            bindingResult.fieldErrors.forEach {
                errors.add(
                    FieldError(
                        field = it.field,
                        value = when (it.rejectedValue) {
                            is String -> it.rejectedValue as String
                            else -> "rejectedValue value is empty"
                        },
                        reason = reason(it)
                    )
                )
            }
        }

        private fun reason(it: org.springframework.validation.FieldError) = when (it.defaultMessage) {
            null -> "defaultMessage value is empty"
            else -> when (it.code) {
                null -> "error code is empty"
                else -> {
                    when {
                        // EnumType miss match 케이스의 경우 자바 관련 시스템 Exception 메시지가 응답되기 때문에 정제된 메시지가 응답되게 변경
                        it.code!!.contains("typeMismatch") -> "올바른 타입이 아닙니다."
                        else -> it.defaultMessage!!
                    }
                }
            }
        }
    }
}


enum class ErrorCode(
    val status: Int,
    val code: String,
    val message: String
) {

    // Common
    INVALID_INPUT_VALUE(400, "C001", " Invalid Input Value"),
    METHOD_NOT_ALLOWED(405, "C002", " Invalid Input Value"),

    HANDLE_ACCESS_DENIED(403, "C006", "Access is Denied"),

    // Member
    EMAIL_DUPLICATION(400, "M001", "Email is Duplication"),
    LOGIN_INPUT_INVALID(400, "M002", "Login input is invalid"),

    SERVER_ERROR(400, "M002", "Login input is invalid"),

    ;
}

fun Int.isSuccessful(): Boolean = this in (200 until 300)

fun Int.isClientError(): Boolean = this in (400 until 500)

fun Int.isServerError(): Boolean = this in (400 until 500)

open class ServiceException(
    errorResponse: ErrorResponse,
    code: ErrorCode
): RuntimeException()

class ApiException(
    errorResponse: ErrorResponse,
    code: ErrorCode
): ServiceException(errorResponse, code)