package com.server.gateway

class ErrorResponse(
    val message: String,
    val status: Int,
    val errors: List<FieldError> = emptyList(),
    val code: String,
) {

    // Error Code 기반 생성자
    constructor(
        message: String? = null,
        code: ErrorCode
    ) : this(
        message = message ?: code.message,
        status = code.status,
        code = code.code
    )
}

class FieldError(
    val field: String,
    val value: String,
    val reason: String
)

enum class ErrorCode(
    val status: Int,
    val code: String,
    val message: String
) {
    FRAME_WORK_INTERNAL_ERROR(500, "C001", "프레임워크 내부 예외"),
    UNDEFINED_ERROR(500, "C002", "정의하지 않은 예외"),
}

class BusinessException(
    override val message: String?,
    val errorCode: ErrorCode
) : RuntimeException()