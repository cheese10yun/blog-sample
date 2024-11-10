package com.example.redis

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.repository.CrudRepository

@RedisHash(value = "coupon")
data class Coupon(
    @Id
    var id: String? = null,
//    @TimeToLive
//    val ttl: Long = 60,
    val discount: Double,
    val code: String,
    val valid: Boolean,
)

interface CouponRepository : CrudRepository<Coupon, String> {

    fun findByCode(code: String): Coupon?

    fun deleteByCode(code: String)

    fun existsByCode(code: String): Boolean
}