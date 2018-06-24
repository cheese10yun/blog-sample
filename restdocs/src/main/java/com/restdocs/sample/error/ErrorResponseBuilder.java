package com.restdocs.sample.error;

import java.util.List;

class ErrorResponseBuilder {

    static ErrorResponse newTypeIncludeErrors(ErrorCode errorCode, List<ErrorResponse.FieldError> errors) {
        return ErrorResponse.builder()
                .code(errorCode.code())
                .status(errorCode.status())
                .message(errorCode.message())
                .errors(errors)
                .build();
    }
}
