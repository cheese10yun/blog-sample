package yun.blog.error.error;

import lombok.Getter;

@Getter
public enum ErrorCode {

  // Common
  INVALID_INPUT_VALUE(400, "C001", "입력값이 올바르지 않습니다."),
  METHOD_NOT_ALLOWED(405, "C002", "올바른 요청이 아닙니다."),
  INTERNAL_SERVER_ERROR(500, "C004", "문제가 발생했습니다. "),

  ;


  private final String code;
  private final String message;
  private int status;

  ErrorCode(final int status, final String code, final String message) {
    this.status = status;
    this.message = message;
    this.code = code;
  }


}
