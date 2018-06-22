package com.cheese.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class CustomJacksonAnnotationTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void whenSerializingUsingCustomAnnotation_thenCorrect() throws JsonProcessingException {
        CustomJacksonAnnotation.BeanWithCustomAnnotation bean
                = new CustomJacksonAnnotation.BeanWithCustomAnnotation(1, "My bean", null);

        String result = objectMapper.writeValueAsString(bean);
        System.out.println(result);

        assertThat(result, containsString("My bean"));
        assertThat(result, containsString("1"));
        assertThat(result, not(containsString("dateCreated")));
    }
}