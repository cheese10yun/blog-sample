package com.example.mongostudy.member

import com.example.mongostudy.mongo.Auditable
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@CompoundIndexes(
    CompoundIndex(name = "email_status", def = "{'email' : 1, 'status': 1}", unique = true)
)
@Document(collection = "members")
class Member(
    @Field(name = "member_id")
    val memberId: String,

    @Field(name = "name")
    var name: String,

    @Field(name = "email")
    val email: String,

    @Field(name = "date_joined")
    val dateJoined: LocalDateTime,

    @Field(name = "date_of_birth")
    val dateOfBirth: LocalDate,

    @Field(name = "phone_number")
    val phoneNumber: String,

    @Field(name = "address")
    val address: Address,

    @Field(name = "status")
    val status: MemberStatus,

    @Field(name = "points_accumulated")
    val pointsAccumulated: BigDecimal,

    @Field(name = "last_purchase_date")
    val lastPurchaseDate: LocalDateTime,


) : Auditable()

enum class MemberStatus {
    ACTIVE, INACTIVE, SUSPENDED
}

data class Address(
    @Field(name = "address")
    val address: String,
    @Field(name = "address_detail")
    val addressDetail: String,
    @Field(name = "zip_code")
    val zipCode: String
)