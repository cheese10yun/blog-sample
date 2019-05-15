package yun.blog.exception;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExceptionApplication {

  public static void main(String[] args) {

    RuntimeException runtimeException;

    SpringApplication.run(ExceptionApplication.class, args);
  }

}
