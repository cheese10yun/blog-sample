package com.cheese.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class JacksonGeneralAnnotationsTest {


    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void whenSerializingUsingJsonFormat_thenCorrect() throws JsonProcessingException, ParseException {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        String toParse = "20-12-2014 02:30:00";
        Date date = df.parse(toParse);
        JacksonGeneralAnnotations.Event event = new JacksonGeneralAnnotations.Event("party", date);

        String result = writeValueAsString(event);

        assertThat(result, containsString(toParse));
    }


    @Test
    public void whenSerializingUsingJsonUnwrapped_thenCorrect() throws JsonProcessingException {
        JacksonGeneralAnnotations.UnwrappedUser.Name name = new JacksonGeneralAnnotations.UnwrappedUser.Name("John", "Doe");
        JacksonGeneralAnnotations.UnwrappedUser user = new JacksonGeneralAnnotations.UnwrappedUser(1, name);
        String result = writeValueAsString(user);

        assertThat(result, containsString("John"));
        assertThat(result, not(containsString("name")));
    }

    @Test
    public void whenSerializingUsingJsonView_thenCorrect()
            throws JsonProcessingException {
        JacksonGeneralAnnotations.Item item = new JacksonGeneralAnnotations.Item(2, "book", "John");
        String result = writeValueAsString(item, JacksonGeneralAnnotations.Views.Public.class);


        System.out.println(result);

        assertThat(result, containsString("book"));
        assertThat(result, containsString("2"));
        assertThat(result, not(containsString("John")));
    }


    @Test
    public void whenSerializingUsingJacksonReferenceAnnotation_thenCorrect() throws JsonProcessingException {
        JacksonGeneralAnnotations.UserWithRef user = new JacksonGeneralAnnotations.UserWithRef(1, "John");
        JacksonGeneralAnnotations.ItemWithRef item = new JacksonGeneralAnnotations.ItemWithRef(2, "book", user);
        user.addItem(item);

        String result = writeValueAsString(item);

        assertThat(result, containsString("book"));
        assertThat(result, containsString("John"));
        assertThat(result, not(containsString("userItems")));
    }

    @Test
    public void whenSerializingUsingJsonFilter_thenCorrect() throws JsonProcessingException {
        JacksonGeneralAnnotations.BeanWithFilter bean = new JacksonGeneralAnnotations.BeanWithFilter(1, "My bean");

        FilterProvider filters = new SimpleFilterProvider().addFilter(
                "myFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept("name")
        );

        String result = new ObjectMapper()
                .writer(filters)
                .writeValueAsString(bean);

        System.out.println(result);

        assertThat(result, containsString("My bean"));
        assertThat(result, not(containsString("id")));
    }


    private String writeValueAsString(Object object) throws JsonProcessingException {
        final String json = objectMapper.writeValueAsString(object);
        System.out.println(json);
        return json;
    }

    private <T> String writeValueAsString(Object object, Class<T> clazz) throws JsonProcessingException {
        final String json = new ObjectMapper()
                .writerWithView(clazz)
                .writeValueAsString(object);
        System.out.println(json);
        return json;
    }
}