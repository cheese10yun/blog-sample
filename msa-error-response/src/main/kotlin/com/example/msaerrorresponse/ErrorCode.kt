package com.example.msaerrorresponse

import com.fasterxml.jackson.annotation.JsonFormat

enum class ErrorCode(val status: Int, val code: String, val message: String) {
    // Common
    INVALID_INPUT_VALUE(400, "C001", " Invalid Input Value"),
    METHOD_NOT_ALLOWED(405, "C002", " Invalid Input Value"),
    ENTITY_NOT_FOUND(400, "C003", " Entity Not Found"),
    INTERNAL_SERVER_ERROR(500, "C004", "Server Error"),
    INVALID_TYPE_VALUE(400, "C005", " Invalid Type Value"),
    SERVICE_ERROR(400, "C006", "Access is Denied"),
}