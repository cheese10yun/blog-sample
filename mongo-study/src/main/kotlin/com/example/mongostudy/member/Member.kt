package com.example.mongostudy.member

import com.example.mongostudy.mongo.Auditable
import com.example.mongostudy.order.Order
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.data.mongodb.core.mapping.DBRef

@CompoundIndexes(
    CompoundIndex(name = "email_status", def = "{'email' : 1, 'status': 1}", unique = true)
)
@Document(collection = "members")
class Member(
    @Field(name = "member_id")
    val memberId: String,

    @Field(name = "content")
    val content: Content
) : Auditable()

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
