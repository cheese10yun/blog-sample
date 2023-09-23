package com.example.restdocssample.member

import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema.Companion.schema
import com.example.restdocssample.SpringWebTestSupport
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class MemberApiYunTest : SpringWebTestSupport() {

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
                        .tag("Member") // 문서에 표시될 태그
                        .summary("Member 조회") // 문서에 표시될 요약정보
                        .description(
                            """
                                                    * 블라블라
                                                    * 블라
                                                    """.trimIndent()
                        ) // 문서에 표시될 상세정보
                        .responseSchema(schema(MemberResponse::class.java.simpleName)) // 문서에 표시될 응답객체 정보
                        .pathParameters(
                            parameterWithName("id").description("Member ID")
                        )
                        .responseFields(
                            // 응답 field 검증 및 문서화
                            fieldWithPath("id").description("ID").type(JsonFieldType.NUMBER),
                            fieldWithPath("name").description("name").type(JsonFieldType.STRING),
                            fieldWithPath("email").description("email").type(JsonFieldType.STRING),
                        )
                        .build()
                )
            )

    }
}