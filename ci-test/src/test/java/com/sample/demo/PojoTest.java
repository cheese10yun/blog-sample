package com.sample.demo;

import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;


public class PojoTest {

    @Test
    public void name() {
        final Pojo pojo = new Pojo();

        pojo.setAge(10);
        pojo.setName("asd");

        assertThat(pojo.getAge()).isEqualTo(10);
        assertThat(pojo.getName()).isEqualTo("asd");
    }
}