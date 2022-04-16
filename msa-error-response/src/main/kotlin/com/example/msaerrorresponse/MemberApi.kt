package com.example.msaerrorresponse

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/members")
class MemberApi(
    private val bookReservationRepository: BookReservationRepository
) {

    @GetMapping
    fun gerMembers(
         pageable: Pageable
    ): Page<BookReservation> {

        return bookReservationRepository.findAll(pageable)
    }

    data class Member(
        val name: String,
        val age: Int
    )
}