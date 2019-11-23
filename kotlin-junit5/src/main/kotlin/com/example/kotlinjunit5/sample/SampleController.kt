package com.example.kotlinjunit5.sample

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sample")
class SampleController(
        private val aRepository: ARepository,
        private val bRepository: BRepository
) {
    @GetMapping
    fun transaction() {
        aRepository.save(A("A"))
        bRepository.save(B("B"))
    }
}