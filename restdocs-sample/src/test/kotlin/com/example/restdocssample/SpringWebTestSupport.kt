package com.example.restdocssample

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter

@SpringBootTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension::class)
@Import(RestDocsConfiguration::class)
class SpringWebTestSupport {

    @Autowired
    protected lateinit var restdocs: RestDocsConfiguration

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var write: RestDocumentationResultHandler

    @BeforeEach
    fun setUp(context: WebApplicationContext, provider: RestDocumentationContextProvider) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .addFilters<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true))
            .apply<DefaultMockMvcBuilder>(
                MockMvcRestDocumentation.documentationConfiguration(
                    provider
                )
            )
            .alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print())
            .alwaysDo<DefaultMockMvcBuilder>(restdocs.write())
            .build()
    }

//    @Test
//    fun name() {
//        mockMvc.perform(
//            post("/custom").contentType(MediaType.APPLICATION_JSON)
//                .content()
//        )
//            .andDo(write.document()
//    }
}