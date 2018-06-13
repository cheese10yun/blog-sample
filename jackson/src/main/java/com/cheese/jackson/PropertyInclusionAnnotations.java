package com.cheese.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PropertyInclusionAnnotations {

    @JsonIgnoreProperties({"id"})
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Getter
    public static class BeanWithIgnore {
        public int id;
        public String name;
    }


    @Getter
    @AllArgsConstructor
    public static class User {
        public int id;
        public Name name;

        @JsonIgnoreType
        @AllArgsConstructor
        public static class Name {
            public String firstName;
            public String lastName;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @AllArgsConstructor
    public static class MyBean {
        public int id;
        public String name;
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @AllArgsConstructor
    public static class PrivateBean {
        private int id;
        private String name;
    }


}
