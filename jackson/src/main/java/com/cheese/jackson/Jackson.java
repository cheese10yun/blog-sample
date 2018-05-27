package com.cheese.jackson;

import com.fasterxml.jackson.annotation.*;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

public class Jackson {

    @Builder
    public static class ExtendableBean {
        public String name;
        private Map<String, String> properties;

        @JsonAnyGetter
        public Map<String, String> getProperties() {
            return properties;
        }
    }

    @JsonPropertyOrder({"name", "id"})
    @Builder
    @Getter
    public static class PropertyOrder {
        private long id;
        private String name;
    }

    @Builder
    public static class MyBean {
        public int id;
        private String name;

        @JsonGetter("name")
        public String getTheName() {
            return name;
        }
    }

    @Builder
    public static class RawBean {
        public String name;

        @JsonRawValue
        public String json;
    }

    public enum TypeEnumWithValue {
        TYPE1(1, "Type A"),
        TYPE2(2, "Type 2");

        private Integer id;
        private String name;

        TypeEnumWithValue(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @JsonValue
        public String getName() {
            return name;
        }
    }


    @Builder
    @JsonRootName(value = "user")
    public static class UserWithRoot {
        public int id;
        public String name;
    }


}
