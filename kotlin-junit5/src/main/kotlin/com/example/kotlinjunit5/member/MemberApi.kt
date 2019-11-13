package com.example.kotlinjunit5.member

import org.springframework.web.bind.annotation.*
import javax.validation.constraints.NotEmpty

@RestController
@RequestMapping("/members")
class MemberApi(
        val memberRepository: MemberRepository
) {

    @PostMapping
    fun create(@RequestBody dto: MemberSignUpRequest) {
        memberRepository.save(dto.toEntity())
    }

    @GetMapping
    fun getMembers(): List<Member> {
        return memberRepository.findAll()
    }
}

data class MemberSignUpRequest(
        @field:NotEmpty
        val name: String,
        @field:NotEmpty
        val email: String
) {

    fun toEntity(): Member {
        return Member(email, name)
    }
}