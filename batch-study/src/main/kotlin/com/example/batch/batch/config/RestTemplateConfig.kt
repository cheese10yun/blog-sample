package com.example.batch.batch.config

import logger
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.RestTemplate
import java.nio.charset.StandardCharsets
import java.time.Duration

@Configuration
class RestTemplateConfig {

    @Bean
    fun paymentRestTemplate(): RestTemplate {
        return RestTemplateBuilder()
            .rootUri("http://localhost:8080")
            .setConnectTimeout(Duration.ofSeconds(30))
            .additionalInterceptors(RestTemplateClientHttpRequestInterceptor())
            .build()
    }
}


class RestTemplateClientHttpRequestInterceptor : ClientHttpRequestInterceptor {

    private val log by logger()

    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {

        loggingRequest(request, body)
        return execution.execute(request, body);
    }

    private fun loggingRequest(request: HttpRequest, body: ByteArray) {
//        log.info("=====Request======")
//        log.info("Headers: {}", request.headers)
//        log.info("Request Method: {}", request.method)
//        log.info("Request URI: {}", request.uri)
//        log.info("Request body: {}",
//            if (body.isEmpty()) null else String(body, StandardCharsets.UTF_8))
//        log.info("=====Request======")
    }

}