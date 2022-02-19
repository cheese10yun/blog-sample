package com.example.api

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

@SpringBootApplication
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}


@Component
class AppRunner(
    private val memberRepository: MemberRepository
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {

        val members = listOf(
            Member("name", "asd@asd.com"),
            Member("name1", "asd1@asd.com"),
            Member("name2", "asd2@asd.com"),
            Member("name3", "asd3@asd.com"),
            Member("name4", "asd4@asd.com")
        )
        memberRepository.saveAll(members)
    }
}