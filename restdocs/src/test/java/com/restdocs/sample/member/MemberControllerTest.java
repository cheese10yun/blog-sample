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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MemberControllerTest {

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

    @Autowired
    private WebApplicationContext context;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    private Email email;
    private Address address;

    @Before
    public void setUp() {
        email = buildEmail();
        address = buildAddress();


        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation))
                .alwaysDo(document("{class-name}/{method-name}/"))
                .build();
    }

    @Test
    public void signUp() throws Exception {

        final SignUpDto signUpDto = SignUpDto.builder()
                .email(email)
                .address(address)
                .build();

        final ResultActions resultActions = requestSignUp(signUpDto);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email.value", is(email.getValue())));

    }

    @Test
    public void signUpFailed() throws Exception {

        final SignUpDto signUpDto = SignUpDto.builder()
                .email(Email.builder().value("test").build())
                .address(address)
                .build();

        final ResultActions resultActions = requestSignUp(signUpDto);

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorCode.INPUT_VALUE_INVALID.message())));

    }

    private ResultActions requestSignUp(SignUpDto signUpDto) throws Exception {
        return mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpDto)))
                .andDo(print());
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