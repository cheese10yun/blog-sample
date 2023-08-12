package com.spring.camp.io

import java.io.IOException
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate


@Configuration
class ClientConfiguration {

    @Bean
    fun partnerClientRestTemplate(): RestTemplate = RestTemplateBuilder()
        .rootUri("http://localhost:8787")
        .errorHandler(RestTemplateErrorHandler())
        .build()

    @Bean
    fun partnerClient(partnerClientRestTemplate: RestTemplate) = PartnerClient(
        restTemplate = partnerClientRestTemplate
    )


//    @Bean
//    @Primary
//    @Profile("test")
//    fun mockPartnerClientService() =
//        mock(PartnerClientService::class.java)!!

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