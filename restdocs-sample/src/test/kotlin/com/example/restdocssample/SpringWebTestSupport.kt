package com.example.restdocssample

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaField
import org.hibernate.validator.constraints.Length
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.constraints.Constraint
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.snippet.Attributes
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@SpringBootTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension::class)
class SpringWebTestSupport {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp(context: WebApplicationContext, provider: RestDocumentationContextProvider) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(MockMvcRestDocumentation.documentationConfiguration(provider))
            .alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print())
            .build()
    }

    protected fun readJson(path: String): String {
        return String(ClassPathResource(path).inputStream.readBytes())
    }

    fun writeRestDocs(): RestDocumentationResultHandler {
        return MockMvcRestDocumentation.document(
            "{class-name}/{method-name}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())
        )
    }

    fun writeOpenApi(resourceSnippetParameters: ResourceSnippetParameters): RestDocumentationResultHandler {
        return MockMvcRestDocumentationWrapper.document(
            "{class-name}/{method-name}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            resource(resourceSnippetParameters)
        )
    }


    fun FieldDescriptor.fieldValidation(property: KProperty<*>): FieldDescriptor {
        // 해당 클래스에서 fieldName으로 필드를 찾습니다.
        val field = property.javaField

        // nullable 판단
        when {
            property.returnType.isMarkedNullable -> this.optional()
            else -> this.required()
        }

        field?.getAnnotation(NotEmpty::class.java)?.let {
            this.notEmpty()
        }

        field?.getAnnotation(NotNull::class.java)?.let {
            // NotNull 어노테이션 처리
            this.notNull()
        }

        field?.getAnnotation(Length::class.java)?.let { length ->
            this.length(length.min, length.max)
        }

        // 해당 필드가 enum인지 확인
        if (isEnumProperty(property)) {
            // Enum 타입 캐스팅 후 enumValues 메소드 호출
            @Suppress("UNCHECKED_CAST")
            val enumClass = property.returnType.classifier as KClass<*>
            this.enumValues(enumClass)
        }


        return this // 변경된 또는 추가된 FieldDescriptor 반환
    }

    fun isEnumProperty(property: KProperty<*>): Boolean {
        return property.returnType.classifier?.let {
            it is KClass<*> && it.isSubclassOf(Enum::class)
        } ?: false
    }

    fun FieldDescriptor.required(): FieldDescriptor {
        val newConstraints = mutableListOf(
            Constraint("javax.validation.constraints.NotNull", emptyMap()),
            Constraint("javax.validation.constraints.NotEmpty", emptyMap())
        )
        return this.addConstraints(newConstraints)
    }

    fun FieldDescriptor.notNull(): FieldDescriptor {
        val newConstraints = mutableListOf(
            Constraint("javax.validation.constraints.NotEmpty", emptyMap())
        )
        return this.addConstraints(newConstraints)
    }

    fun FieldDescriptor.notEmpty(): FieldDescriptor {
        val newConstraints = mutableListOf(
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

    fun FieldDescriptor.enumValues(enumClass: KClass<*>): FieldDescriptor {
        @Suppress("UNCHECKED_CAST")
        val enumConstants = (enumClass.java as Class<Enum<*>>).enumConstants
        return this.attributes(Attributes.key("enumValues").value(enumConstants.map { it.name }))
    }


    fun FieldDescriptor.addConstraints(newConstraints: List<Constraint>): FieldDescriptor {
        val constraints = this.attributes["validationConstraints"] as? MutableList<Constraint> ?: mutableListOf()

        constraints.addAll(newConstraints)
        return this.attributes(Attributes.key("validationConstraints").value(constraints))
    }

    fun fieldWithPageResponse(): Array<FieldDescriptor> {
        return listOf(
            fieldWithPath("total_elements").description("total_elements").type(JsonFieldType.NUMBER).fieldValidation(OpenApiPageResponse::totalElements),
            fieldWithPath("total_pages").description("total_pages").type(JsonFieldType.NUMBER).fieldValidation(OpenApiPageResponse::totalPages),
            fieldWithPath("size").description("size").type(JsonFieldType.NUMBER).fieldValidation(OpenApiPageResponse::size),
            fieldWithPath("number").description("number").type(JsonFieldType.NUMBER).fieldValidation(OpenApiPageResponse::number),
            fieldWithPath("number_of_elements").description("number_of_elements").type(JsonFieldType.NUMBER).fieldValidation(OpenApiPageResponse::numberOfElements),
            fieldWithPath("last").description("last").type(JsonFieldType.BOOLEAN).fieldValidation(OpenApiPageResponse::last),
            fieldWithPath("first").description("first").type(JsonFieldType.BOOLEAN).fieldValidation(OpenApiPageResponse::first),
            fieldWithPath("empty").description("empty").type(JsonFieldType.BOOLEAN).fieldValidation(OpenApiPageResponse::empty),
            fieldWithPath("content[0]").description("content").type(JsonFieldType.ARRAY).fieldValidation(OpenApiPageResponse::content)
        ).toTypedArray()
    }
}

data class OpenApiPageResponse(
    val totalElements: Int,
    val totalPages: Int,
    val size: Int,
    val number: Int,
    val numberOfElements: Int,
    val last: Boolean,
    val first: Boolean,
    val empty: Boolean,
    val content: List<*> = emptyList<Any>()
)