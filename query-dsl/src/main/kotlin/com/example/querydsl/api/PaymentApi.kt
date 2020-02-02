package com.example.querydsl.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.awt.print.Pageable
import java.math.BigDecimal

@RestController
@RequestMapping("/payment")
class PaymentApi {

    @GetMapping
    fun get(@RequestParam amount: BigDecimal, pageable: Pageable) {




    }

}