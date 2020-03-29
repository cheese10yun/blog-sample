package com.example.querydsl.repository.coupon

import com.example.querydsl.SpringBootTestSupport
import com.example.querydsl.domain.Coupon
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Transactional
internal class CouponRepositoryTest(
    private val couponRepository: CouponRepository
) : SpringBootTestSupport() {

    @Test
    internal fun save() {

        val coupons = (1..20).map {
            Coupon(BigDecimal.TEN)
        }

        couponRepository.saveAll(coupons)
    }
}