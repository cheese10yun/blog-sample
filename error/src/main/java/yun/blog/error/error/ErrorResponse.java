package yun.blog.error.error;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.validation.BindingResult;

@Getter
abstract class ErrorResponse {

  private String message;
  private int status;
  private String code;
  private List<FieldError> errors;
  private LocalDateTime timestamp = LocalDateTime.now();

  private Detail detail;

  ErrorResponse() {
  }

  public ErrorResponse(final ErrorCode code, final List<FieldError> errors, Exception e,
      HttpServletRequest request) {
    this.message = code.getMessage();
    this.status = code.getStatus();
    this.errors = errors;
    this.code = code.getCode();
    this.detail = Detail.of(e, request);
  }

  public ErrorResponse(final ErrorCode code, Exception e, HttpServletRequest request) {
    this.message = code.getMessage();
    this.status = code.getStatus();
    this.code = code.getCode();
    this.errors = new ArrayList<>();
    this.detail = Detail.of(e, request);
  }

  abstract ErrorResponse of(final ErrorCode code, final BindingResult bindingResult, Exception e,
      HttpServletRequest request);


  abstract ErrorResponse of(final ErrorCode code, Exception e, HttpServletRequest request);


  @Getter
  public static class FieldError {

    private final String field;
    private final String value;
    private final String reason;

    private FieldError(final String field, final String value, final String reason) {
      this.field = field;
      this.value = value;
      this.reason = reason;
    }

    protected static List<FieldError> of(final BindingResult bindingResult) {
      final List<org.springframework.validation.FieldError> fieldErrors = bindingResult
          .getFieldErrors();
      return fieldErrors.stream()
          .map(error -> new FieldError(
              error.getField(),
              error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
              error.getDefaultMessage()))
          .collect(Collectors.toList());
    }
  }

  @Getter
  public static class Detail {

    private String className;
    private String methodName;
    private long lineNumber;
    private String exceptionName;
    private String errorMessage;
    private String path;
    private String httpMethod;

    private Detail(String exceptionName, String errorMessage, String path, String httpMethod) {
      this.exceptionName = exceptionName;
      this.errorMessage = errorMessage;
      this.path = path;
      this.httpMethod = httpMethod;

    }

    private Detail(String exceptionName, String errorMessage, String path, String httpMethod,
        String className, String methodName, int lineNumber) {

      this.exceptionName = exceptionName;
      this.errorMessage = errorMessage;
      this.path = path;
      this.httpMethod = httpMethod;
      this.className = className;
      this.methodName = methodName;
      this.lineNumber = lineNumber;
    }


    static Detail of(Exception e, HttpServletRequest request) {

      final StackTraceElement[] stackTrace = e.getStackTrace();
      final String exceptionName = e.getClass().getSimpleName();
      final String errorMessage = e.getMessage();
      final String path = request.getServletPath();
      final String httpMethod = request.getMethod();

      if (stackTrace.length != 0) {
        final StackTraceElement stackTraceElement = stackTrace[0];
        String className = stackTraceElement.getClassName();
        String methodName = stackTraceElement.getMethodName();
        int lineNumber = stackTraceElement.getLineNumber();

        return new Detail(exceptionName, errorMessage, path, httpMethod, className, methodName,
            lineNumber);
      }

      return new Detail(exceptionName, errorMessage, path, httpMethod);
    }


  }


}
