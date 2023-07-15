package com.example.tobyspring;

class SimpleHelloService implements HelloService {

    @Override
    public String sayHello(String msg) {
        return "Hello Servlet " + msg;
    }
}
