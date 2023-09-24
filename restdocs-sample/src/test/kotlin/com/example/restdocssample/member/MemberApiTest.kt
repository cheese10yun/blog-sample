package com.example.restdocssample.member

import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema
import com.example.restdocssample.ErrorResponse
import com.example.restdocssample.SpringWebTestSupport
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

//@Disabled
class MemberApiTest: SpringWebTestSupport(){

//    @Test
//    fun member_page_test() {
//        mockMvc.perform(
//            get("/api/members")
//                .param("size", "10")
//                .param("page", "0")
//                .contentType(MediaType.APPLICATION_JSON)
//        )
//            .andExpect(MockMvcResultMatchers.status().isOk())
//            .andDo(
//                write.document(
//                    requestParameters(
//                        parameterWithName("size").optional().description("size"),
//                        parameterWithName("page").optional().description("page")
//                    )
//                )
//            )
//    }

    @Test
    fun member_get() {
        // 조회 API -> 대상의 데이터가 있어야 합니다.
        mockMvc.perform(
            get("/api/members/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(
                writeOpenApi(
                    ResourceSnippetParameters
                        .builder()
                        .tag("members")
                        .summary("Member 조회")
                        .description(
                            """
                            * 블라블라
                            * 블라
                            """.trimIndent()
                        )
                        .responseSchema(Schema.schema(MemberResponse::class.java.simpleName)) // 문서에 표시될 응답객체 정보
                        .pathParameters(
                            parameterWithName("id").description("Member ID")
                        )
                        .responseFields(
                            fieldWithPath("id").description("ID").type(JsonFieldType.NUMBER).required().minimum(3).maximum(3222),
                            fieldWithPath("name").description("asd").type(JsonFieldType.STRING).required().length(2, 10),
                            fieldWithPath("email").description("email").type(JsonFieldType.STRING).required().length(2, 10),
                            fieldWithPath("status").description("status").type("enum").enumValues(MemberStatus::class).required()
                        )
                        .build()
                )
            )
    }

    @Test
    fun member_get_bad_request() {
        // 잘못된 요청 시뮬레이션 -> 예를 들어, 잘못된 ID나 누락된 필수 정보 등을 전달
        mockMvc.perform(
            get("/api/members/{id}", 5L)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andDo(
                writeOpenApi(
                    ResourceSnippetParameters
                        .builder()
                        .tag("members")
                        .summary("Member 조회 - 잘못된 요청")
                        .description("잘못된 ID 또는 필요한 정보가 누락된 경우에 대한 응답 예시.")
                        .responseSchema(Schema.schema(ErrorResponse::class.java.simpleName))  // 여기에 오류 응답의 스키마를 지정
                        .responseFields(
                            fieldWithPath("message").description("에러 코드").type(JsonFieldType.STRING),
                            fieldWithPath("status").description("에러 메시지").type(JsonFieldType.NUMBER),
                            fieldWithPath("code").description("에러 메시지").type(JsonFieldType.STRING),
                            fieldWithPath("errors").description("에러 메시지").type(JsonFieldType.ARRAY),
                            fieldWithPath("timestamp").description("에러 메시지").type(JsonFieldType.STRING),
                        )
                        .build()
                )
            )
    }

    @Test
    fun member_create() {
        mockMvc.perform(
            post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(readJson("/json/member-api/member-create.json"))
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(
                writeOpenApi(
                    ResourceSnippetParameters
                        .builder()
                        .tag("members")
                        .summary("Member 생성")
                        .description(
                            """
                            * 블라블라
                            * 블라
                            """.trimIndent()
                        )
//                        .responseSchema(Schema.schema(MemberResponse::class.java.simpleName)) // 문서에 표시될 응답객체 정보
                        .requestSchema(Schema.schema(MemberSignUpRequest::class.java.simpleName)) // 문서에 표시될 응답객체 정보
                        .requestFields(
                            fieldWithPath("name").description("asd").type(JsonFieldType.STRING).required().length(2, 10),
                            fieldWithPath("email").description("email").type(JsonFieldType.STRING).required().length(2, 10),
                            fieldWithPath("status").description("status").type("enum").enumValues(MemberStatus::class).required()
                        )
                        .build()
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