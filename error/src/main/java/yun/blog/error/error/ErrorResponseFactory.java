package yun.blog.error.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class ErrorResponseFactory {


  @Value("${spring.profiles.active}")
  private String activeProfile;

  private ErrorResponseLocal errorResponseLocal = new ErrorResponseLocal();
  private ErrorResponseProd errorResponseProd = new ErrorResponseProd();


  public ErrorResponse getInstance() {

    switch (activeProfile) {
      case "local":
        return errorResponseLocal;

      default:
        return errorResponseProd;

    }


  }


}
