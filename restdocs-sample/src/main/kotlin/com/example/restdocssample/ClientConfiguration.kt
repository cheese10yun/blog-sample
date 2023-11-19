package com.example.restdocssample

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import java.io.IOException

@Configuration
class ClientConfiguration {

    @Bean
    fun restTemplate(): RestTemplate = RestTemplateBuilder()
        .rootUri("http://localhost:8787")
        .errorHandler(RestTemplateErrorHandler())
        .build()
}

class RestTemplateErrorHandler : ResponseErrorHandler {
    @Throws(IOException::class)
    override fun hasError(response: ClientHttpResponse): Boolean {
        return false
    }

    @Throws(IOException::class)
    override fun handleError(response: ClientHttpResponse) {
    }
}

