package com.example.boot3mongo.member

import com.example.boot3mongo.mongo.Auditable
import com.example.boot3mongo.order.Order
import java.math.BigDecimal
import java.time.LocalDateTime
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@CompoundIndexes(
    CompoundIndex(name = "email_status", def = "{'email' : 1, 'status': 1}", unique = true)
)
@Document(collection = "members")
class Member(
    @Field(name = "name")
    val name: String,

    @Field(name = "address")
    val address: Address,

    @Field(name = "member_id")
    val memberId: String,

    @Field(name = "email")
    val email: String,

    @Field(name = "status")
    val status: MemberStatus,

    @Field(name = "points_accumulated")
    val pointsAccumulated: BigDecimal,

    @Field(name = "date_joined")
    val dateJoined: LocalDateTime,
) : Auditable() {

    companion object {
        val DOCUMENT_NAME = "members"
    }
}

data class Content(
    @Field(name = "content")
    val content: String,

    @DBRef
    val order: Order,
)

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
