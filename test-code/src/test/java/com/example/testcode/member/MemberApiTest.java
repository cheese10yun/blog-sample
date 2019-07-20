package com.example.testcode.member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Transactional
public class MemberApiTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected ResourceLoader resourceLoader;

  @Test
  public void 회원가입테스트() throws Exception {

    //given
    final MemberSingUpRequest dto = new MemberSingUpRequest("yun", "yun@asd.com");

    //when
    final ResultActions resultActions = mvc.perform(post("/members")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(objectMapper.writeValueAsString(dto)))
        .andDo(print());

    //then
    resultActions
        .andExpect(status().isOk());

  }


  @Test
  public void json_파일로테스트() throws Exception {
    //given
    final String requestBody = readJson("classpath:member-singup.json");

    //when
    final ResultActions resultActions = mvc.perform(post("/members")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(requestBody))
        .andDo(print());

    //then
    resultActions
        .andExpect(status().isOk());

  }

  @Test
  public void default_접근지시자_를이용한_테스트() throws Exception {
    //given
    final MemberSingUpRequest dto = MemberSignUpRequestBuilder.build("yun", "yun@asd.com");

    //when
    final ResultActions resultActions = mvc.perform(post("/members")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(objectMapper.writeValueAsString(dto)))
        .andDo(print());

    //then
    resultActions
        .andExpect(status().isOk());

  }

  @Test
  public void member_page_test() throws Exception {
    //given
    //dataSetUp("classpath:member_page_test.sql"); 로직은 없습니다.

    //when
    final ResultActions resultActions = mvc.perform(get("/members")
        .contentType(MediaType.APPLICATION_JSON_UTF8))
        .andDo(print());

    //then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("content").exists())
        .andExpect(jsonPath("pageable").exists())
        .andExpect(jsonPath("pageable").exists())
        .andExpect(jsonPath("numberOfElements").value("4"));

  }

  protected String readJson(final String path) throws IOException {
    final InputStream inputStream = resourceLoader.getResource(path).getInputStream();
    return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
  }
}