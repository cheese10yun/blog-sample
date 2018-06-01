package com.cheese.jackson;

import com.fasterxml.jackson.databind.InjectableValues;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DeserializationAnnotationsTest {

//    json to object

    private String dir;

    @Before
    public void setUp() {
        dir = System.getProperty("user.dir") + "/json/";
    }

    @Test
    public void whenDeserializingUsingJsonCreator_thenCorrect() throws IOException {

        DeserializationAnnotations.BeanWithCreator bean = new ObjectMapper()
                .readerFor(DeserializationAnnotations.BeanWithCreator.class)
                .readValue(getJsonFile("JsonCreator.json"));

        assertEquals("My bean", bean.name);
    }

    @Test
    public void whenDeserializingUsingJsonInject_thenCorrect() throws IOException {

        InjectableValues inject = new InjectableValues.Std().addValue(int.class, 1);

        DeserializationAnnotations.BeanWithInject bean = new ObjectMapper().reader(inject)
                .forType(DeserializationAnnotations.BeanWithInject.class)
                .readValue(getJsonFile("JacksonInject.json"));

        assertEquals("My bean", bean.name);
        assertEquals(1, bean.id);
    }

    @Test
    public void whenDeserializingUsingJsonAnySetter_thenCorrect() throws IOException {

        DeserializationAnnotations.ExtendableBean bean = new ObjectMapper()
                .readerFor(DeserializationAnnotations.ExtendableBean.class)
                .readValue(getJsonFile("JsonAnySetter.json"));

        assertEquals("My bean", bean.name);
        assertEquals("val2", bean.getProperties().get("attr2"));
    }

    @Test
    public void whenDeserializingUsingJsonSetter_thenCorrect() throws IOException {


        DeserializationAnnotations.MyBean bean = new ObjectMapper()
                .readerFor(DeserializationAnnotations.MyBean.class)
                .readValue(getJsonFile("JsonSetter.json"));


        assertEquals("My bean", bean.getTheName());
    }





    private File getJsonFile(String fileName) {
        return new File(dir + fileName);
    }

}