package com.example.reactorstudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@SpringBootApplication
public class ReactorStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactorStudyApplication.class, args);
    }

//    @Bean
//    public RouterFunction<ServerResponse> routes()

}
