package com.cheese.jackson;

import com.fasterxml.jackson.databind.MapperFeature;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class DisableJacksonAnnotationTest {


    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void whenDisablingAllAnnotations_thenAllDisabled() throws IOException {
        DisableJacksonAnnotation.MyBean bean = new DisableJacksonAnnotation.MyBean(1, null);

        mapper.disable(MapperFeature.USE_ANNOTATIONS);
        String result = mapper.writeValueAsString(bean);
        System.out.println(result);

        assertThat(result, containsString("1"));
        assertThat(result, containsString("name"));
    }

}