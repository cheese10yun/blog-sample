package com.cheese.jackson;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

public class CustomJacksonAnnotation {
    @CustomAnnotation
    public static class BeanWithCustomAnnotation {
        public int id;
        public String name;
        public Date dateCreated;

        public BeanWithCustomAnnotation(int id, String name, Date dateCreated) {
            this.id = id;
            this.name = name;
            this.dateCreated = dateCreated;
        }
    }
}

@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "id", "dateCreated"})
@interface CustomAnnotation {
}
