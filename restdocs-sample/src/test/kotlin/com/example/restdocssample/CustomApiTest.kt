package com.example.restdocssample

import com.epages.restdocs.apispec.ResourceDocumentation.resource
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class CustomApiTest : SpringWebTestSupport() {

    @Test
    fun asdasdsa() {


        mockMvc.perform(
            post("/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {
                          "name": "name",
                          "email": "email"
                        }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk())
            .andDo(document("carts-create", resource("Create a cart")));
//            .andDo(
//                write.document(
//                    requestFields(
//                        fieldWithPath("email").description("The Member's email address"),
//                        fieldWithPath("name").description("The Member's name"),
//                    ),
//                    responseFields(
//                        fieldWithPath("email").description("The Member's email address"),
//                        fieldWithPath("name").description("The Member's name"),
//                    )
//                )
//            )
//            .andExpect(jsonPath("$.email", `is`(notNullValue())))
//            .andExpect(jsonPath("$.name", `is`(notNullValue())))
    }
}