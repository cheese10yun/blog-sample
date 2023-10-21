package com.example.mongostudy.member

import com.example.mongostudy.mongo.Auditable
import com.example.mongostudy.mongo.MongoCustomRepositorySupport
import com.example.mongostudy.order.Order
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.repository.MongoRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@CompoundIndexes(
    CompoundIndex(name = "memberName_email", def = "{'memberName' : 1, 'email': 1}", unique = true)
)
@Document(collection = "members")
class Member(
    @Field(name = "member_id")
    val memberId: String,

    @Field(name = "member_name")
    val memberName: String,

    @Field(name = "email")
    val email: String,

    @Field(name = "date_joined")
    val dateJoined: LocalDateTime,

    @Field(name = "date_of_birth")
    val dateOfBirth: LocalDate,

    @Field(name = "phone_number")
    val phoneNumber: String,

    @Field(name = "address")
    val address: String,

    @Field(name = "membership_status")
    val membershipStatus: MembershipStatus,

    @Field(name = "points_accumulated")
    val pointsAccumulated: BigDecimal,

    @Field(name = "last_purchase_date")
    val lastPurchaseDate: LocalDateTime
) : Auditable()

enum class MembershipStatus {
    ACTIVE, INACTIVE, SUSPENDED
}

interface MemberRepository : MongoRepository<Member, ObjectId>, MemberCustomRepository

interface MemberCustomRepository

class MemberCustomRepositoryImpl(mongoTemplate: MongoTemplate) : MongoCustomRepositorySupport<Member>(
    Member::class.java,
    mongoTemplate
)
