package yun.blog.error.error;

import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

  private final ErrorResponseFactory errorResponseFactory;

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
    log.error("handleEntityNotFoundException", e);
    final ErrorResponse instance = errorResponseFactory.getInstance();
    final ErrorResponse response = instance.of(ErrorCode.INTERNAL_SERVER_ERROR, e, request);
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }


  /**
   * javax.validation.Valid or @Validated 으로 binding error 발생시 발생한다. HttpMessageConverter 에서 등록한
   * HttpMessageConverter binding 못할경우 발생 주로 @RequestBody, @RequestPart 어노테이션에서 발생
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e, HttpServletRequest request) {
    log.error("handleMethodArgumentNotValidException", e);


    final ErrorResponse instance = errorResponseFactory.getInstance();

    final ErrorResponse response = instance
        .of(ErrorCode.INVALID_INPUT_VALUE, e.getBindingResult(), e, request);


    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }


  /**
   * 지원하지 않은 HTTP method 호출 할 경우 발생
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
    log.error("handleHttpRequestMethodNotSupportedException", e);
    final ErrorResponse instance = errorResponseFactory.getInstance();
    final ErrorResponse response = instance.of(ErrorCode.METHOD_NOT_ALLOWED, e, request);
    return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
  }

}
