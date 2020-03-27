package com.example.querydsl.repository.coupon

import com.example.querydsl.domain.Coupon
import org.springframework.data.jpa.repository.JpaRepository

interface CouponRepository : JpaRepository<Coupon, Long> {
}

