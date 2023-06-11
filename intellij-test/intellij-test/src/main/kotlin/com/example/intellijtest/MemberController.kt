package com.example.intellijtest

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberRegistrationService: MemberRegistrationService,
    private val memberRepository: MemberRepository
) {

    @PostMapping
    fun register(
        @RequestBody @Valid dto: MemberRegistrationRequest
    ) {
        // ...
        // 간단한 validation 외에 각종 검증...
        memberRegistrationService.register(dto, true)
    }

    @GetMapping
    fun getMembers(): List<Member> {
        return memberRepository.findAll().filterNotNull()
    }
}

//@MemberRegistrationForm
data class MemberRegistrationRequest(
    @field:NotEmpty
    val firstName: String,
    @field:NotEmpty
    val lastName: String,
    @field:Email
    val email: String
)