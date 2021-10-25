package com.example.exposedstudy

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional


@Transactional
class MemberTest(

) : ExposedTestSupport() {

    @Autowired
    private lateinit var memberRepository: MemberRepository
    
    @Test
    fun `jpa transactional test`() {
        val members = (1..10).map {
            Member(it.toString())
        }

        memberRepository.saveAll(members)
    }
}