package com.cheese.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class PolymorphicTypeHandlingAnnotationsTest {

    private ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void whenSerializingPolymorphic_thenCorrect() throws JsonProcessingException {
        PolymorphicTypeHandlingAnnotations.Zoo.Dog dog = new PolymorphicTypeHandlingAnnotations.Zoo.Dog("lacy");
        PolymorphicTypeHandlingAnnotations.Zoo zoo = new PolymorphicTypeHandlingAnnotations.Zoo(dog);

        String result = objectMapper.writeValueAsString(zoo);
        assertThat(result, containsString("type"));
        assertThat(result, containsString("dog"));
    }

    @Test
    public void whenDeserializingPolymorphic_thenCorrect_Cat() throws IOException {
        String cat = "{\n" +
                "    \"animal\":{\n" +
                "        \"name\":\"lacy\",\n" +
                "        \"type\":\"cat\"\n" +
                "    }\n" +
                "}";

        PolymorphicTypeHandlingAnnotations.Zoo zoo = new ObjectMapper()
                .readerFor(PolymorphicTypeHandlingAnnotations.Zoo.class)
                .readValue(cat);

        assertEquals("lacy", zoo.animal.getName());
        assertEquals(PolymorphicTypeHandlingAnnotations.Zoo.Cat.class, zoo.animal.getClass());
    }

    @Test
    public void whenDeserializingPolymorphic_thenCorrect_Dog() throws IOException {
        String dog = "{\n" +
                "    \"animal\": {\n" +
                "        \"type\": \"dog\",\n" +
                "        \"name\": \"yun\",\n" +
                "        \"barkVolume\": 0\n" +
                "    }\n" +
                "}\n";

        PolymorphicTypeHandlingAnnotations.Zoo zoo = new ObjectMapper()
                .readerFor(PolymorphicTypeHandlingAnnotations.Zoo.class)
                .readValue(dog);

        assertEquals("yun", zoo.animal.getName());
        assertEquals(PolymorphicTypeHandlingAnnotations.Zoo.Dog.class, zoo.animal.getClass());
    }

}