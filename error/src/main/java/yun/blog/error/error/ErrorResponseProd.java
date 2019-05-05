package yun.blog.error.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.validation.BindingResult;

public class ErrorResponseProd extends ErrorResponse {


  ErrorResponseProd() {
  }

  private ErrorResponseProd(ErrorCode code,
      List<FieldError> errors, Exception e, HttpServletRequest request) {
    super(code, errors, e, request);
  }

  private ErrorResponseProd(ErrorCode code, Exception e,
      HttpServletRequest request) {
    super(code, e, request);
  }

  ErrorResponseProd of(final ErrorCode code, final BindingResult bindingResult,
      Exception e, HttpServletRequest request) {
    return new ErrorResponseProd(code, FieldError.of(bindingResult), e, request);
  }


  ErrorResponseProd of(final ErrorCode code, Exception e, HttpServletRequest request) {
    return new ErrorResponseProd(code, e, request);
  }


  @Override
  public String getMessage() {
    return super.getMessage();
  }

  @Override
  public int getStatus() {
    return super.getStatus();
  }

  @Override
  public String getCode() {
    return super.getCode();
  }

  @Override
  public List<FieldError> getErrors() {
    return super.getErrors();
  }

  @Override
  public LocalDateTime getTimestamp() {
    return super.getTimestamp();
  }

  @Override
  @JsonIgnore
  public Detail getDetail() {
    return super.getDetail();
  }
}
