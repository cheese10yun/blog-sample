package com.example.springkotlin.global

import com.example.springkotlin.domain.member.dao.MemberRepository
import com.example.springkotlin.domain.member.domain.Member
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class AppRunner(val memberRepository: MemberRepository) : ApplicationRunner {


    override fun run(args: ApplicationArguments?) {
        val members = listOf(
                Member("yun@asd.com", "yun"),
                Member("yun1@asd.com", "yun"),
                Member("yun2@asd.com", "yun"),
                Member("yun3@asd.com", "yun"),
                Member("yun4@asd.com", "yun")
        )
        memberRepository.saveAll(members)
    }
}