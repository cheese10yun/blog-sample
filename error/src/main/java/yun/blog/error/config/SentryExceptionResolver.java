package yun.blog.error.config;

import io.sentry.Sentry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

public class SentryExceptionResolver implements HandlerExceptionResolver, Ordered {


  @Override
  public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) {

    Sentry.init("https://7fc62c7632714376bfa5503f1ac911ac@sentry.io/1452785"); //여기서 센트리를 초기화시킨다.
    Sentry.capture(ex); //여기서 Exception을 센트리로 보내버린다.

    return null;
  }

  @Override
  public int getOrder() {
    return Integer.MIN_VALUE;
  }
}
