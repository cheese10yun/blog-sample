package com.example.kotlinjunit5.member

import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberRepository: MemberRepository
) {

    fun updateName(name: String, id: Long): Member {
        val member = memberRepository.findById(id).orElseThrow { IllegalArgumentException("$id is not existed") }
        memberRepository.findByName(name)
        member.updateName(name)
        return member
    }
}