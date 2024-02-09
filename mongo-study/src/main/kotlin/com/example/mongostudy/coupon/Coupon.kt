package com.example.mongostudy.coupon

import com.example.mongostudy.mongo.Auditable
import com.example.mongostudy.mongo.MongoCustomRepositorySupport
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.repository.MongoRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Document(collection = "coupons")
class Coupon(
    @Indexed(unique = true)
    @Field(name = "coupon_id")
    val couponId: String,

    @Field(name = "coupon_name")
    val couponName: String,

    @Field(name = "discount_amount")
    val discountAmount: BigDecimal,

    @Field(name = "expiry_date")
    val expiryDate: LocalDate,

    @Field(name = "status")
    val status: CouponStatus,

    @Field(name = "member_id")
    val memberId: String,

    @Field(name = "issued_date")
    val issuedDate: LocalDateTime,

    @Field(name = "used_date")
    val usedDate: LocalDateTime,

    @Field(name = "coupon_type")
    val couponType: String,

    @Field(name = "minimum_order_value")
    val minimumOrderValue: BigDecimal
) : Auditable()

enum class CouponStatus {
    ACTIVE, EXPIRED, USED
}

interface CouponRepository : MongoRepository<Coupon, ObjectId>, CouponCustomRepository

interface CouponCustomRepository

//class CouponCustomRepositoryIml(mongoTemplate: MongoTemplate) : CouponCustomRepository, MongoCustomRepositorySupport<Coupon>(
//    Coupon::class.java,
//    mongoTemplate
//)