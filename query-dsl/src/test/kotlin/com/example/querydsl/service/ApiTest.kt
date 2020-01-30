package com.example.querydsl.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestTemplate
import kotlin.properties.Delegates.notNull


@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class ApiTest(
        private val restTemplate: RestTemplate,
        private val memberService: MemberService

) {


    private var server: MockRestServiceServer by notNull()

    @BeforeEach
    internal fun setUp() {
        server = MockRestServiceServer.createServer(restTemplate)
    }


    @Test
    internal fun `api test`() {
        server.expect(requestTo("https://www.naver.com/"))
                .andRespond(
                        withSuccess("123123", MediaType.APPLICATION_JSON))

        val get = memberService.get()
        println(get)
    }
}