package com.gradle.sample.member;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(MemberApi.class)
public class MemberApiTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void getMember() throws Exception {

        //given

        //when
        final ResultActions actions = requestGetMember("1");

        //then
        actions
                .andExpect(status().isOk());
    }

    private ResultActions requestGetMember(String id) throws Exception {
        return mvc.perform(get("/members/{id}", id)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print());
    }
}