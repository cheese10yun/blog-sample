package com.example.jpanplus1

import com.example.jpanplus1.member.MemberRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AppRunner(val memberRepository: MemberRepository) : ApplicationRunner {

    @Transactional
    override fun run(args: ApplicationArguments?) {
        val members = memberRepository.findAll()

        for (member in members) {
            println(member)

            val orders = member.orders

            for (order in orders) {

                println(order)
            }
        }
    }
}