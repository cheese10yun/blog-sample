package com.example.msaerrorresponse

import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserApi {

    @PostMapping
    fun register(
        @RequestBody @Valid dto: UserRegistrationRequest
    ): UserRegistrationRequest {
        return dto
    }
}

data class UserRegistrationRequest(
    @field:NotEmpty
    val name: String,
    @field:Email
    val email: String
)