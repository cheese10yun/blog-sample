package com.example.kotlinjpa.member

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/members")
class MemberApi(private val memberRepository: MemberRepository) {


    @GetMapping
    fun createMember(): Member {
        val member = Member(email = "asd@asd.com", name = "yun")
        return memberRepository.save(member)
    }

}