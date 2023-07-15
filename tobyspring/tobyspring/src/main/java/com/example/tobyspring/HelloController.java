package com.example.tobyspring;

import org.springframework.web.bind.annotation.*;

import java.util.Objects;

//@RestController
@RequestMapping("/hello")
public class HelloController {

    private final HelloService helloService;

    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }


    @GetMapping
    @ResponseBody
    public String hello(@RequestParam("name") String msg) {
        return helloService.sayHello(Objects.requireNonNull(msg));
    }
}



