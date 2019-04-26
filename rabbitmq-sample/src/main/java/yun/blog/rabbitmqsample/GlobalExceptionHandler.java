package yun.blog.rabbitmqsample;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    log.error("handleException", e);
    log.error("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    return new ResponseEntity<>(new ErrorResponse(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Getter
  private class ErrorResponse {

    private String name = "";
  }

}
