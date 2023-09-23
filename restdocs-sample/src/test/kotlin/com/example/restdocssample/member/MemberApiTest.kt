package com.example.restdocssample.member

import com.epages.restdocs.apispec.ResourceDocumentation
import com.example.restdocssample.SpringWebTestSupport
import com.example.restdocssample.field
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.restdocs.request.RequestDocumentation.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class MemberApiTest: SpringWebTestSupport(){

    @Test
    fun member_page_test() {
        mockMvc.perform(
            RestDocumentationRequestBuilders.get("/api/members")
                .param("size", "10")
                .param("page", "0")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(
                write.document(
                    requestParameters(
                        parameterWithName("size").optional().description("size"),
                        parameterWithName("page").optional().description("page")
                    )
                )
            )
    }

    @Test
    fun member_get() {
        // 조회 API -> 대상의 데이터가 있어야 합니다.
        val resource = ResourceDocumentation.resource("member find")
        mockMvc.perform(
            RestDocumentationRequestBuilders.get("/api/members/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
//            .andDo(document("member get", resource));
            .andDo(
                write.document(
                    pathParameters(
                        parameterWithName("id").description("Member ID")
                    ),
                    responseFields(
                        fieldWithPath("id").description("ID"),
                        fieldWithPath("name").description("name"),
                        fieldWithPath("email").description("email")
                    )
                )
            )
    }

    @Test
    fun member_create() {
        mockMvc.perform(
            RestDocumentationRequestBuilders.post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(readJson("/json/member-api/member-create.json"))
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(
                write.document(
                    requestFields(
                        fieldWithPath("name").description("name").attributes(field("length", "10")),
                        fieldWithPath("email").description("email").attributes(field("length", "30")),
                        fieldWithPath("status").description("Code Member Status 참조")
                    )
                )
            )
    }

//    @Test
//    fun member_modify() {
//        mockMvc.perform(
//            RestDocumentationRequestBuilders.put("/api/members/{id}", 1)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(readJson("/json/member-api/member-modify.json"))
//        )
//            .andExpect(MockMvcResultMatchers.status().isOk())
//            .andDo(
//                write.document(
//                    pathParameters(
//                        parameterWithName("id").description("Member ID")
//                    ),
//                    requestFields(
//                        fieldWithPath("name").description("name").attributes(field("length", "10"))
//                    )
//                )
//            )
//    }
//
//
//    @Test
//    fun member_create_글자_length_실패() {
//        mockMvc.perform(
//            RestDocumentationRequestBuilders.post("/api/members")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(readJson("/json/member-api/member-create-invalid.json"))
//        )
//            .andExpect(MockMvcResultMatchers.status().isBadRequest())
//    }
//
//    @Test
//    fun member_modify_글자_length_실패() {
//        mockMvc.perform(
//            RestDocumentationRequestBuilders.put("/api/members/{id}", 1)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(readJson("/json/member-api/member-modify-invalid.json"))
//        )
//            .andExpect(MockMvcResultMatchers.status().isBadRequest())
//    }


}