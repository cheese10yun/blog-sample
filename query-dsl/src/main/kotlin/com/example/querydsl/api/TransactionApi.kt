package com.example.querydsl.api

import com.example.querydsl.service.CouponService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transaction")
class TransactionApi(
    private val couponService: CouponService
) {

    @GetMapping
    fun transactional(@RequestParam i: Int) {
        couponService.something(i)
    }
}