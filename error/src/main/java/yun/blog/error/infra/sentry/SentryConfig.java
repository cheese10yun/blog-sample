package yun.blog.error.infra.sentry;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import yun.blog.error.config.SentryExceptionResolver;

@Component
public class SentryConfig {

//  @Bean
//  public ServletContextInitializer sentryServletContextInitializer() {
//    return new io.sentry.spring.SentryServletContextInitializer();
//  }

  @Bean
  public HandlerExceptionResolver sentryExceptionResolver() {
    return new SentryExceptionResolver();
  }

}
