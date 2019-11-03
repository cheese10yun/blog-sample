package com.example.kotlinjunit5.member

import org.springframework.web.bind.annotation.*

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
        val name: String,
        val email: String
) {

    fun toEntity(): Member {
        return Member(email, name)
    }
}