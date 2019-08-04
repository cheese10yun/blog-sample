package com.example.springkotlin.member

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/members")
class MemberApi constructor(private var memberRepository: MemberRepository) {

    @GetMapping
    fun createMember(): Member {

        val member = Member("email@asd.com", "name");
        return memberRepository.save(member)

    }


}