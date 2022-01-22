package com.example.msaerrorresponse

import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/a-service")
class AServiceApi(
    private val userRegistrationService: UserRegistrationService
) {

    @PostMapping
    fun aService(@RequestBody dto: UserRegistrationRequest) =
        userRegistrationService.register(dto)

}

data class UserRegistrationRequest(
    @field:NotEmpty
    val name: String,
    @field:Email
    val email: String
)