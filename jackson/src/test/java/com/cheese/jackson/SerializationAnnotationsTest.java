package com.cheese.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class SerializationAnnotationsTest {

    private ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void JsonPropertyOrder() {
        final Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        final SerializationAnnotations.ExtendableBean object = SerializationAnnotations.ExtendableBean.builder()
                .name("yun")
                .properties(map)
                .build();

        toJson(object);
    }

    @Test
    public void JsonGetter() {
        final SerializationAnnotations.MyBean object = SerializationAnnotations.MyBean.builder()
                .id(1)
                .name("yun")
                .build();

        toJson(object);
    }

    @Test
    public void PropertyOrder() {
        final SerializationAnnotations.PropertyOrder object = SerializationAnnotations.PropertyOrder.builder()
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

        final SerializationAnnotations.RawBean object = SerializationAnnotations.RawBean.builder()
                .name("yun")
                .json(json)
                .build();

        toJson(object);

    }

    @Test
    public void JsonValue() {
        final SerializationAnnotations.TypeEnumWithValue object = SerializationAnnotations.TypeEnumWithValue.TYPE1;

        toJson(object);

    }

    @Test
    public void JsonRootName() {
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final SerializationAnnotations.UserWithRoot object = SerializationAnnotations.UserWithRoot.builder()
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