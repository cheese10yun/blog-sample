package com.example.redis

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

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

interface CouponRepository : CrudRepository<Coupon, String>, CustomCouponRepository {

    fun findByCode(code: String): Coupon?

    fun existsByCode(code: String): Boolean

    // 할인율이 특정 값보다 큰 쿠폰 조회
//    fun findByDiscountGreaterThan(discount: Double): List<Coupon>

    // 특정 할인율에 해당하는 쿠폰 조회
    fun findByDiscount(discount: Double): List<Coupon>

    // 할인율이 특정 범위 내에 있는 쿠폰 조회
    fun findByDiscountBetween(minDiscount: Double, maxDiscount: Double): List<Coupon>

    // 유효한 쿠폰만 조회
    fun findByValid(valid: Boolean = true): List<Coupon>

    // 코드가 특정 패턴으로 시작하는 쿠폰 조회
    fun findByCodeStartingWith(prefix: String): List<Coupon>

    // 코드가 특정 패턴으로 끝나는 쿠폰 조회
    fun findByCodeEndingWith(suffix: String): List<Coupon>

    // 코드에 특정 문자열이 포함된 쿠폰 조회
    fun findByCodeContaining(keyword: String): List<Coupon>

    // 유효한 쿠폰 중 할인율이 특정 값보다 높은 쿠폰 조회
    fun findByValidAndDiscountGreaterThan(valid: Boolean, discount: Double): List<Coupon>

    // 특정 할인율 이하의 유효한 쿠폰 존재 여부 확인
    fun existsByValidAndDiscountLessThan(valid: Boolean, discount: Double): Boolean

}


interface CustomCouponRepository {
    fun findCouponsByDiscountGreaterThan(discount: Double): List<Coupon>
    fun findCouponByCode(code: String): Coupon?
}


@Repository
class CustomCouponRepositoryImpl(
    private val redisTemplate: RedisTemplate<String, Coupon>
) : CustomCouponRepository {

    override fun findCouponsByDiscountGreaterThan(discount: Double): List<Coupon> {
        val opsForHash = redisTemplate.opsForHash<String, Coupon>()
        return opsForHash.entries("coupon").values.filter { it.discount > discount }
    }

    override fun findCouponByCode(code: String): Coupon? {
        val hashOps = redisTemplate.opsForHash<String, Coupon>()
        val cursor = hashOps.scan("coupon", ScanOptions.scanOptions().match("*$code*").build())

        cursor.use {
            if (cursor.hasNext()) {
                return cursor.next().value
            }
        }
        return null
    }
}