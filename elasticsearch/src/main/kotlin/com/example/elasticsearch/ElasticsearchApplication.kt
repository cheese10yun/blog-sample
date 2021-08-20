package com.example.elasticsearch

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

@SpringBootApplication
class ElasticsearchApplication

fun main(args: Array<String>) {
    runApplication<ElasticsearchApplication>(*args)
}


@Component
class AppRunner(
    val memberRepository: MemberRepository
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        (1..20).map {
            Member(
                id = it.toLong(),
                name = "name-$it",
                email = "sample-$it@asd.comm"
            )
        }
            .also {
                memberRepository.saveAll(it)
            }
    }
}