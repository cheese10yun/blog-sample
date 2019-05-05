package yun.blog.error.error;

import java.time.LocalDateTime;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.validation.BindingResult;

public class ErrorResponseLocal extends ErrorResponse {

  ErrorResponseLocal() {
  }

  private ErrorResponseLocal(ErrorCode code,
      List<FieldError> errors, Exception e, HttpServletRequest request) {
    super(code, errors, e, request);
  }

  private ErrorResponseLocal(ErrorCode code, Exception e,
      HttpServletRequest request) {
    super(code, e, request);
  }

  ErrorResponseLocal of(final ErrorCode code, final BindingResult bindingResult,
      Exception e, HttpServletRequest request) {
    return new ErrorResponseLocal(code, FieldError.of(bindingResult), e, request);
  }


  ErrorResponseLocal of(final ErrorCode code, Exception e, HttpServletRequest request) {
    return new ErrorResponseLocal(code, e, request);
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
  public Detail getDetail() {
    return super.getDetail();
  }
}
