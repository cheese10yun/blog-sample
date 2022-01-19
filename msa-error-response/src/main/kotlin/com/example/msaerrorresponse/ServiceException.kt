package com.example.msaerrorresponse

abstract class ServiceException(
    val errorCode: ErrorCode,
    override val message: String
) : RuntimeException()

class ApiException(
    errorCode: ErrorCode,
    val errorResponse: ErrorResponse
) : ServiceException(errorCode, errorCode.message)