package com.example.demo

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class FuelClientTest {

    private lateinit var mockServer: ClientAndServer

    @BeforeAll
    fun startServer() {
        this.mockServer = ClientAndServer.startClientAndServer(8080)
    }

    @AfterAll
    fun stopServer() {
        this.mockServer.stop()
    }


    @Test
    internal fun `getSample test`() {
        //given
        val client = FuelClient()
        val responseBody = """
                    {
                      "foo": "foo",
                      "bar": "bar"
                    }
                """.trimIndent()

        //when
        mockServer.`when`(
            HttpRequest.request()
                .withMethod("GET")
                .withPath("/sample")
        ).respond(
            HttpResponse.response()
                .withBody(responseBody)
                .withStatusCode(200)
        )

        //then
        val sample = client.getSample()

        then(sample.foo).isEqualTo("foo")
        then(sample.bar).isEqualTo("bar")
    }
}