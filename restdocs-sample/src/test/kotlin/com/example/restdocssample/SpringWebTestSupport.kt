package com.example.restdocssample

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper
import com.epages.restdocs.apispec.ResourceDocumentation
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import kotlin.reflect.KClass
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
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.snippet.Attributes
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

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
//            .addFilters<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true))
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
        return this.attributes(Attributes.key("enumValues").value(enumClass.java.enumConstants.map { it.name }))
    }

    fun FieldDescriptor.addConstraints(newConstraints: List<Constraint>): FieldDescriptor {
        val constraints = this.attributes["validationConstraints"] as? MutableList<Constraint> ?: mutableListOf()

        constraints.addAll(newConstraints)
        return this.attributes(Attributes.key("validationConstraints").value(constraints))
    }
}