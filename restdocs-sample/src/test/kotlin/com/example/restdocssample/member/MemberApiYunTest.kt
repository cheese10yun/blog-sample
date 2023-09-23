package com.example.restdocssample.member

import com.epages.restdocs.apispec.ConstrainedFields
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema.Companion.schema
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

        val fields = ConstrainedFields(MemberResponse::class.java)
        val withPath = fields.withPath(MemberResponse::name.name)


        val listOf: List<Constraint> = listOf(
            Constraint("javax.validation.constraints.NotNull", emptyMap()),
            Constraint("javax.validation.constraints.NotEmpty", emptyMap()),
        )


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
                            fieldWithPath("id").description("ID").type(JsonFieldType.NUMBER).required().minimum(3).maximum(3222),
                            fieldWithPath("name").description("asd").type(JsonFieldType.STRING).required().length(2, 10),
                            fieldWithPath("email").description("email").type(JsonFieldType.STRING).required().length(2, 10),
                            fieldWithPath("status").description("status").type("enum").enumValues(MemberStatus::class).required()
                        )
                        .build()
                )
            )

    }


    fun FieldDescriptor.required(): FieldDescriptor {
        val constraints = this.attributes["validationConstraints"] as? MutableList<Constraint>
        val newConstraints = mutableListOf(
            Constraint("javax.validation.constraints.NotNull", emptyMap()),
            Constraint("javax.validation.constraints.NotEmpty", emptyMap())
        )

        return if (constraints != null) {
            // 기존 constraints에 새로운 constraints를 추가
            constraints.addAll(newConstraints)

            this.attributes(key("validationConstraints").value(constraints))
        } else {
            this.attributes(key("validationConstraints").value(newConstraints))
        }
    }

    fun FieldDescriptor.minimum(value: Int): FieldDescriptor {
        val constraints = this.attributes["validationConstraints"] as? MutableList<Constraint>
        val newConstraints = mutableListOf(
            Constraint("javax.validation.constraints.Min", mapOf("value" to value)),
        )

        return if (constraints != null) {
            // 기존 constraints에 새로운 constraints를 추가
            constraints.addAll(newConstraints)
            this.attributes(key("validationConstraints").value(constraints))
        } else {
            this.attributes(key("validationConstraints").value(newConstraints))
        }
    }

    fun FieldDescriptor.maximum(value: Int): FieldDescriptor {
        val constraints = this.attributes["validationConstraints"] as? MutableList<Constraint>
        val newConstraints = mutableListOf(
            Constraint("javax.validation.constraints.Max", mapOf("value" to value)),
        )

        return if (constraints != null) {
            // 기존 constraints에 새로운 constraints를 추가
            constraints.addAll(newConstraints)
            this.attributes(key("validationConstraints").value(constraints))
        } else {
            this.attributes(key("validationConstraints").value(newConstraints))
        }
    }


    fun FieldDescriptor.length(min: Int, max: Int): FieldDescriptor {
        val constraints = this.attributes["validationConstraints"] as? MutableList<Constraint>
        val newConstraints = mutableListOf(
            Constraint("org.hibernate.validator.constraints.Length", mapOf("min" to min, "max" to max)),
        )

        return if (constraints != null) {
            // 기존 constraints에 새로운 constraints를 추가
            constraints.addAll(newConstraints)
            this.attributes(key("validationConstraints").value(constraints))
        } else {
            this.attributes(key("validationConstraints").value(newConstraints))
        }
    }



    fun FieldDescriptor.max(value: Int): FieldDescriptor {
        return this.attributes(key("validationConstraints").value(listOf(Constraint("javax.validation.constraints.Max", mapOf("value" to value)))))
    }

    fun <T : Enum<T>> FieldDescriptor.enumValues(enumClass: KClass<T>): FieldDescriptor {
        return this.attributes(key("enumValues").value(enumClass.java.enumConstants.map { it.name }))
    }
}
