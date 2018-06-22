package com.cheese.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public class DisableJacksonAnnotation {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"name", "id"})
    public static class MyBean {
        public int id;
        public String name;

        public MyBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
