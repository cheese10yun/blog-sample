package com.blog.properties;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class AntiSamplePropertiesRunner implements ApplicationRunner {

    private final Environment env;

    @Override
    public void run(ApplicationArguments args)  {
        final String email = env.getProperty("sample.email");
        final String name = env.getProperty("sample.name");
        final int age = Integer.valueOf(env.getProperty("sample.age"));
        final boolean auth = Boolean.valueOf(env.getProperty("sample.auth"));

        log.info("==================");
        log.info(email);
        log.info(name);
        log.info(String.valueOf(age));
        log.info(String.valueOf(auth));
        log.info("==================");

    }
}
