package com.example.msaerrorresponse

abstract class ServiceException(
    val errorCode: ErrorCode,
    override val message: String
) : RuntimeException()

class ApiException(
    val errorResponse: ErrorResponse
) : ServiceException(ErrorCode.SERVICE_ERROR, ErrorCode.SERVICE_ERROR.message) // Error Code가 실질적으로 진행하는 것은 없지만 필수 값이라 전달