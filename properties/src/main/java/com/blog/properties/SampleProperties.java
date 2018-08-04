package com.blog.properties;


import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "sample")
@Getter
@Setter
public class SampleProperties {
    private String email;
    private String name;
    private int age;
    private boolean auth;
}

