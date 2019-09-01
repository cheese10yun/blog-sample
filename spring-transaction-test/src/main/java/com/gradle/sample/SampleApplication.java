package com.gradle.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SampleApplication {

    public static void main(String[] args) {
        //for jenkins test
        SpringApplication.run(SampleApplication.class, args);
    }

}
