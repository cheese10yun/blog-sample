package com.spring.test.member

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val memberRepository: MemberRepository
) {

    @Transactional
    fun create(name: String, email: String): Member {
        return memberRepository.save(Member(name, email))
    }
}