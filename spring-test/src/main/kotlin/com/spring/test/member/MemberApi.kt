package com.spring.test.member

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/members")
class MemberApi(
    private val memberRepository: MemberRepository,
    private val memberService: MemberService
) {

    @GetMapping
    fun getMembers(pageable: Pageable): Page<Member> {
        return memberRepository.findAll(pageable)
    }

    @PostMapping
    fun createMember(@RequestBody dto: MemberRequest) {
        memberService.create(dto.name, dto.email)
    }
}

data class MemberRequest(
    val name: String,
    val email: String
)