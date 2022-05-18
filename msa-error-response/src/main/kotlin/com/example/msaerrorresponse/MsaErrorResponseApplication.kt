package com.example.msaerrorresponse

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableAsync
class MsaErrorResponseApplication

fun main(args: Array<String>) {
    runApplication<MsaErrorResponseApplication>(*args)
}

@Component
class Runner(
    private val bookReservationRepository: BookReservationRepository
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        val toList = (1..20).map {
            BookReservation(
                bookId = it.toLong(),
                bookStatus = "OPEN",
                userId = it.toLong(),
            )
        }
            .toList()

        bookReservationRepository.saveAll(toList)

    }
}