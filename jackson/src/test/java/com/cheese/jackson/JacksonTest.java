package com.cheese.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Builder;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class JacksonTest {

    private ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void JsonPropertyOrder() {
        final Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        final Jackson.ExtendableBean object = Jackson.ExtendableBean.builder()
                .name("yun")
                .properties(map)
                .build();

        toJson(object);
    }

    @Test
    public void JsonGetter() {
        final Jackson.MyBean object = Jackson.MyBean.builder()
                .id(1)
                .name("yun")
                .build();

        toJson(object);
    }

    @Test
    public void PropertyOrder() {
        final Jackson.PropertyOrder object = Jackson.PropertyOrder.builder()
                .id(1)
                .name("name")
                .build();

        toJson(object);
    }

    @Test
    public void JsonRawValue() {

        final String json = "{\n" +
                "  \"attr\":false\n" +
                "}";

        final Jackson.RawBean object = Jackson.RawBean.builder()
                .name("yun")
                .json(json)
                .build();

        toJson(object);

    }

    @Test
    public void JsonValue() {
        final Jackson.TypeEnumWithValue object = Jackson.TypeEnumWithValue.TYPE1;

        toJson(object);

    }

    @Test
    public void JsonRootName() {
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final Jackson.UserWithRoot object = Jackson.UserWithRoot.builder()
                .id(1)
                .name("yun")
                .build();

        toJson(object);

    }

    private String toJson(Object object) {
        try {
            final String json = objectMapper.writeValueAsString(object);

            print(json);
            return json;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void print(String json) {
        System.out.println(json);
    }
}