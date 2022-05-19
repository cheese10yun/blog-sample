package com.example.msaerrorresponse

import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import org.slf4j.LoggerFactory
import org.springframework.cloud.sleuth.Tracer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/a-service")
class AServiceApi(
    private val userRegistrationService: UserRegistrationService,
    private val tracer: Tracer,
    private val userClient: UserClient
) {

    private val log = LoggerFactory.getLogger(javaClass)




    @PostMapping
    fun aService(@RequestBody dto: UserRegistrationRequest) =
        userRegistrationService.register(dto)


    @GetMapping("/test")
    fun test(){
        val currentSpan = tracer.currentSpan()
        val nextSpan = tracer.nextSpan()
        val span = currentSpan ?: nextSpan
        val context = span.context()



        userClient.getUser(1)
    }


}

data class UserRegistrationRequest(
    @field:NotEmpty
    val name: String,
    @field:Email
    val email: String
)