package com.example.tobyspring;

import org.springframework.stereotype.Component;

@Component
class SimpleHelloService implements HelloService {

    @Override
    public String sayHello(String msg) {
        return "Hello Servlet " + msg;
    }
}
