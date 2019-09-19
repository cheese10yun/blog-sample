package com.gradle.sample.member;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gradle.sample.test.IntegrationApiTest;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

public class MemberApiIntegrationTest extends IntegrationApiTest {


  @Test
  public void getMember() throws Exception {

    //given

    //when
    final ResultActions actions = requestGetMember("1");

    //then
    actions
        .andExpect(status().isOk())
        .andDo(document(identifier,
            pathParameters(parameterWithName("id").description("Member Id")),
            responseFields(
                fieldWithPath("id").description("The Member's email address"),
                fieldWithPath("name").description("The Member's address city"),
                fieldWithPath("email").description("The Member's address street")
            )));

  }

  private ResultActions requestGetMember(String id) throws Exception {
    return mvc.perform(get("/members/{id}", id)
        .contentType(MediaType.APPLICATION_JSON_UTF8))
        .andDo(print());
  }
}