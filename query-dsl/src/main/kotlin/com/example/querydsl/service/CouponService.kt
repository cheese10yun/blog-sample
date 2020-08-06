package com.example.querydsl.service

import com.example.querydsl.domain.Coupon
import com.example.querydsl.repository.coupon.CouponRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponService(
    private val couponRepository: CouponRepository
) {

    fun something(i: Int) {
        save(i)
    }

    @Transactional
    fun save(i: Int) {
        (1..i).map {
            if (it == 20) {
                throw RuntimeException("$i ....")
            }
            couponRepository.save(Coupon(it.toBigDecimal()))
        }
    }
}