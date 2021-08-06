package com.service.member.user

import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserApi(
    val environment: Environment,
    val userSignUpService: UserSignUpService
) {

    @GetMapping("/welcome")
    fun welcome(): String? {
        return environment.getProperty("getting.message")
    }

    @PostMapping
    fun signUp(@RequestBody @Valid dto: UserSignUpRequest) {
        userSignUpService.signUp(dto)
    }

}

data class UserSignUpRequest(
    @field:Email
    val email: String,
    @field:NotEmpty
    val name: String,
    @field:NotEmpty
    val password: String
) {
    fun toEntity() =
        User(
            email = this.email,
            name = this.name,
            password = "password",
            userid = UUID.randomUUID().toString()
        )

}