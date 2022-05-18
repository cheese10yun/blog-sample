package com.example.msaerrorresponse

import org.slf4j.LoggerFactory
import org.springframework.cloud.sleuth.Tracer
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/members")
class MemberApi(
        private val bookReservationRepository: BookReservationRepository,
        private val aaa: AAA,
        private val tracer: Tracer
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun gerMembers(
            pageable: Pageable
    ): Page<BookReservation> {
//        log.info("gerMembers current thead id : ${Thread.currentThread().id}")

        val traceId = (tracer.currentSpan() ?: tracer.nextSpan()).context().traceId()
        log.error("traceId: $traceId")
//        aaa.async()

        return bookReservationRepository.findAll(pageable)
    }

    data class Member(
            val name: String,
            val age: Int
    )
}

@Service
class AAA {
    private val log = LoggerFactory.getLogger(javaClass)

    @Async
    fun async() {

        log.info("async current thead id : ${Thread.currentThread().id}")
    }
}