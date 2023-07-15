package com.example.tobyspring;

import java.util.Objects;

public class HelloController {

    private final HelloService helloService;

    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }


    public String hello(String msg) {
        return helloService.sayHello(Objects.requireNonNull(msg));
    }
}



