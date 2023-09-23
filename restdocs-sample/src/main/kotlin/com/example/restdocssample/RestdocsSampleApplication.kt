package com.example.restdocssample

import com.example.restdocssample.member.Member
import com.example.restdocssample.member.MemberRepository
import com.example.restdocssample.member.MemberStatus
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

@SpringBootApplication
class RestdocsSampleApplication

fun main(args: Array<String>) {
    runApplication<RestdocsSampleApplication>(*args)
}


@Component
class DataSetup(
    private val memberRepository: MemberRepository
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        memberRepository.saveAll(
            listOf(
                Member("yun@bbb.com", "yun", MemberStatus.BAN),
                Member("jin@bbb.com", "jin", MemberStatus.NORMAL),
                Member("han@bbb.com", "han", MemberStatus.NORMAL),
                Member("jo@bbb.com", "jo", MemberStatus.LOCK)
            )
        )
    }
}
