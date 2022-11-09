package com.example.querydsl.service

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.web.client.RestTemplate
import java.util.function.Consumer
import kotlin.properties.Delegates.notNull


@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class ApiServiceTest(
    private val restTemplate: RestTemplate, // (1)
    private val apiService: ApiService // (2)

) {
    private var server: MockRestServiceServer by notNull()

    @BeforeEach
    internal fun setUp() {
        server = MockRestServiceServer.createServer(restTemplate) // (3)
    }


    @Test
    internal fun `api test`() {
        //given
        val path = "/team-api-response.json"
        val name = "team"
        server
            .expect(
                requestTo("http://localhost:8080/teams?name=$name")
            )// (4)
            .andExpect(method(HttpMethod.GET)) // (5)
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ClassPathResource(path, javaClass))
            )// (6)

        //when
        // (7)
        val teams = apiService.getTeam("team")

        //then
        // (8)
        for (team in teams) {
            println(team)
        }

        // (9)
        then(teams).hasSize(10)
        then(teams).allSatisfy(
            Consumer {
                then(it.name).startsWith("team")
            }
        )
    }
}