package com.cheese.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class PropertyInclusionAnnotationsTest {

    private ObjectMapper objectMapper = new ObjectMapper();
    private String dir;

    @Before
    public void setUp() {
        dir = System.getProperty("user.dir") + "/json/";
    }


    @Test
    public void BeanWithIgnore() {
        final PropertyInclusionAnnotations.BeanWithIgnore beanWithIgnore = PropertyInclusionAnnotations.BeanWithIgnore.builder()
                .id(1)
                .name("yun")
                .build();

        final String json = writeValueAsString(beanWithIgnore);

        assertThat(json, containsString("yun"));
        assertThat(json, not(containsString("id")));
    }

    @Test
    public void User() {
        PropertyInclusionAnnotations.User.Name name = new PropertyInclusionAnnotations.User.Name("yun", "kim");
        PropertyInclusionAnnotations.User user = new PropertyInclusionAnnotations.User(1, name);

        final String json = writeValueAsString(user);
        assertThat(json, containsString("id"));
        assertThat(json, not(containsString("name")));
    }

    @Test
    public void MyBean() {
        PropertyInclusionAnnotations.MyBean myBean = new PropertyInclusionAnnotations.MyBean(1, null);

        final String json = writeValueAsString(myBean);

        assertThat(json, containsString("id"));
        assertThat(json, not(containsString("name")));
    }

    @Test
    public void PrivateBean() {
        PropertyInclusionAnnotations.PrivateBean bean = new PropertyInclusionAnnotations.PrivateBean(1, "yun");

        final String json = writeValueAsString(bean);
        assertThat(json, containsString("id"));
        assertThat(json, (containsString("name")));
    }

    private String writeValueAsString(Object obj) {
        try {
            final String json = objectMapper.writeValueAsString(obj);
            System.out.println(json);
            return json;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private File getJsonFile(String fileName) {
        return new File(dir + fileName);
    }


}