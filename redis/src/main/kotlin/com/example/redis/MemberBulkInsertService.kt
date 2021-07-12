package com.example.redis

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberBulkInsertService(
    val memberRepository: MemberRepository
) {

    @Transactional
    fun save(members: List<Member>) {
        memberRepository.saveAll(members)
    }
}