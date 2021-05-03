package com.example.springtransaction

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component


@SpringBootApplication
class SpringTransactionApplication

fun main(args: Array<String>) {
    runApplication<SpringTransactionApplication>(*args)
}

@Component
class AppSetup(
    private val bookRepository: BookRepository
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        bookRepository.saveAll(
            (1..20).map { Book("asd") }
                .toList()
        )
    }
}

