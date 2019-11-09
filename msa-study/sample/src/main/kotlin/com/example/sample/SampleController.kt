package com.example.sample

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand
import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
@RequestMapping("hello")
class SampleController(val restTemplate: RestTemplate) {

    @HystrixCommand(threadPoolKey = "helloThreadPool")
    fun helloRemoteServiceCall(firstName: String, lastName: String): String {
        val restExchange = restTemplate.exchange(
                "http://logical-service-id/name/ca[{firstName}/{lastName}]",
                HttpMethod.GET, null, String::class.java, firstName, lastName
        )
        return restExchange.body!!
    }

    @GetMapping("/{firstName}/{lastName}")
    fun hello(@PathVariable firstName: String, @PathVariable lastName: String): String {
        return helloRemoteServiceCall(firstName, lastName)
    }
}