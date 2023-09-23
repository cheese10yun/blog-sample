package com.example.restdocssample.member

import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema.Companion.schema
import com.example.restdocssample.ErrorResponse
import com.example.restdocssample.SpringWebTestSupport
import kotlin.reflect.KClass
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.constraints.Constraint
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.snippet.Attributes.key
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
                        .tag("Member")
                        .summary("Member 조회")
                        .description(
                            """
                            * 블라블라
                            * 블라
                            """.trimIndent()
                        )
                        .responseSchema(schema(MemberResponse::class.java.simpleName)) // 문서에 표시될 응답객체 정보
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
                        .tag("Member")
                        .summary("Member 조회 - 잘못된 요청")
                        .description("잘못된 ID 또는 필요한 정보가 누락된 경우에 대한 응답 예시.")
                        .responseSchema(schema(ErrorResponse::class.java.simpleName))  // 여기에 오류 응답의 스키마를 지정
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


    fun FieldDescriptor.required(): FieldDescriptor {
        val newConstraints = mutableListOf(
            Constraint("javax.validation.constraints.NotNull", emptyMap()),
            Constraint("javax.validation.constraints.NotEmpty", emptyMap())
        )
        return this.addConstraints(newConstraints)
    }

    fun FieldDescriptor.minimum(value: Int): FieldDescriptor {
        val newConstraints = mutableListOf(
            Constraint("javax.validation.constraints.Min", mapOf("value" to value))
        )
        return this.addConstraints(newConstraints)
    }

    fun FieldDescriptor.maximum(value: Int): FieldDescriptor {
        val newConstraints = mutableListOf(
            Constraint("javax.validation.constraints.Max", mapOf("value" to value))
        )
        return this.addConstraints(newConstraints)
    }

    fun FieldDescriptor.length(min: Int, max: Int): FieldDescriptor {
        val newConstraints = mutableListOf(
            Constraint("org.hibernate.validator.constraints.Length", mapOf("min" to min, "max" to max))
        )
        return this.addConstraints(newConstraints)
    }

    fun <T : Enum<T>> FieldDescriptor.enumValues(enumClass: KClass<T>): FieldDescriptor {
        return this.attributes(key("enumValues").value(enumClass.java.enumConstants.map { it.name }))
    }

    fun FieldDescriptor.addConstraints(newConstraints: List<Constraint>): FieldDescriptor {
        val constraints = this.attributes["validationConstraints"] as? MutableList<Constraint> ?: mutableListOf()

        constraints.addAll(newConstraints)
        return this.attributes(key("validationConstraints").value(constraints))
    }
}
