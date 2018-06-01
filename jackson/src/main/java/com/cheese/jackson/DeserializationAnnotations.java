package com.cheese.jackson;

import com.fasterxml.jackson.annotation.*;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

public class DeserializationAnnotations {

    public static class BeanWithCreator {
        public int id;
        public String name;

        @JsonCreator
        public BeanWithCreator(
                @JsonProperty("id") int id,
                @JsonProperty("theName") String name
        ) {
            this.id = id;
            this.name = name;
        }
    }

    //json에 없는데 추가
//@JacksonInject 는 JSON 데이터가 아닌 주입에서 값을 가져올 속성을 나타내는 데 사용됩니다 .
    public static class BeanWithInject {
        @JacksonInject
        public int id;

        public String name;
    }


    public static class ExtendableBean {
        public String name;
        private Map<String, String> properties = new HashMap<>();


        @JsonAnySetter
        public void setProperties(String key, String value) {
            properties.put(key, value);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        public Map<String, String> getProperties() {
            return properties;
        }
    }


    public static class MyBean {
        public int id;
        private String name;

        @JsonSetter("name")
        public void setTheName(String name) {
            this.name = name;
        }

        public String getTheName() {
            return this.name;
        }
    }

}
