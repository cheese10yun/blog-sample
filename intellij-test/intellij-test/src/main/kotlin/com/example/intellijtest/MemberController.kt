package com.example.intellijtest

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberRegistrationService: MemberRegistrationService
) {

    @PostMapping
    fun register(
        @RequestBody @Valid dto: MemberRegistrationRequest
    ) {
        // ...
        // 간단한 validation 외에 각종 검증...
        memberRegistrationService.register(dto)
    }
}

@MemberRegistrationForm
data class MemberRegistrationRequest(
    @field:NotEmpty
    val firstName: String,
    @field:NotEmpty
    val lastName: String,
    @field:NotEmpty
    val email: String
)

@Service
class MemberRegistrationService(
) {

    fun register(dto: MemberRegistrationRequest) {

    }

}