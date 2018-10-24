package com.restdocs.sample.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restdocs.sample.error.ErrorCode;
import com.restdocs.sample.member.dto.SignUpDto;
import com.restdocs.sample.model.Address;
import com.restdocs.sample.model.Email;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MemberControllerTest {

    @Rule
//    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired
    private WebApplicationContext context;
    private ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    private Email email;
    private Address address;

    private RestDocumentationResultHandler documentationHandler;

    @Before
    public void setUp() {
        email = buildEmail();
        address = buildAddress();

        this.documentationHandler = document("{method-name}",
                preprocessRequest(removeHeaders("Authorization")),
                preprocessResponse(prettyPrint()));


        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation))
//                .alwaysDo(document("{class-name}/{method-name}/"))
                .alwaysDo(documentationHandler)
                .build();
    }

    @Test
    public void sign_up() throws Exception {

        // given
        final SignUpDto signUpDto = SignUpDto.builder()
                .email(email)
                .address(address)
                .build();

        // when
        final ResultActions resultActions = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpDto)))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
//                .andDo(document(
//                        "sign_up"
//                ))
                .andExpect(jsonPath("$.email.value", is(email.getValue())));

    }

    @Test
    public void sign_up_fail() throws Exception {

        final SignUpDto signUpDto = SignUpDto.builder()
                .email(Email.builder().value("test").build())
                .address(address)
                .build();

        final ResultActions resultActions = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpDto)))
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorCode.INPUT_VALUE_INVALID.message())))
                .andDo(document("sign_up_fail"))
        ;

    }

    @Test
    public void get_member() throws Exception {
        mockMvc.perform(get("/members/{id}", 1L)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("get_member",
                        pathParameters(
                                parameterWithName("id").description("Document's id")
                        ),
                        responseFields(
                                fieldWithPath("email.value").description("The user`s email address"),
                                fieldWithPath("address.city").description("The user`s email address"),
                                fieldWithPath("address.street").description("The user`s email address"),
                                fieldWithPath("address.zipCode").description("The user`s email address")
                        )
                ))
                .andExpect(jsonPath("$.email.value", is("test@test.com")))
        ;


    }

    @Test
    public void get_members() throws Exception {
        mockMvc.perform(get("/members?page=1")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("get_members",

//                        responseFields(
//                                fieldWithPath("email.value").description("The user`s email address"),
//                                fieldWithPath("address.city").description("The user`s email address"),
//                                fieldWithPath("address.street").description("The user`s email address"),
//                                fieldWithPath("address.zipCode").description("The user`s email address")
//                        ),
                        requestParameters(
                                parameterWithName("page").description("The page to retrieve")
                        )

                ))
                .andExpect(jsonPath("$.email.value", is("test@test.com")))
        ;


    }


    private Email buildEmail() {
        return Email.builder()
                .value("test@test.com")
                .build();
    }


    private Address buildAddress() {
        return Address.builder()
                .city("city")
                .street("street")
                .zipCode("zipCode")
                .build();
    }
}