package com.example.mongostudy.member

import com.example.mongostudy.mongo.Auditable
import com.example.mongostudy.mongo.MongoCustomRepositorySupport
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
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

interface MemberRepository : MongoRepository<Member, ObjectId>, MemberCustomRepository, QuerydslPredicateExecutor<Member>

interface MemberCustomRepository {
    fun findByMemberName(name: String): List<Member>
    fun findByEmail(email: String): List<Member>
    fun findActiveMembers(): List<Member>
    fun findMembersWithPointsOver(points: BigDecimal): List<Member>
}

class MemberCustomRepositoryImpl(mongoTemplate: MongoTemplate) : MemberCustomRepository, MongoCustomRepositorySupport<Member>(
    Member::class.java,
    mongoTemplate
) {
    override fun findByMemberName(name: String): List<Member> {
        val query = Query(Criteria.where("member_name").`is`(name))
        return mongoTemplate.find(query, Member::class.java)
    }

    override fun findByEmail(email: String): List<Member> {
        val query = Query(Criteria.where("email").`is`(email))
        return mongoTemplate.find(query, Member::class.java)
    }

    override fun findActiveMembers(): List<Member> {
        val query = Query(Criteria.where("membership_status").`is`(MembershipStatus.ACTIVE))
        return mongoTemplate.find(query, Member::class.java)
    }

    override fun findMembersWithPointsOver(points: BigDecimal): List<Member> {
        val query = Query(Criteria.where("points_accumulated").gt(points))
        return mongoTemplate.find(query, Member::class.java)
    }

    fun findPageBy(
        pageable: Pageable,
        name: String? = null,
        email: String? = null,
        dateJoinedFrom: LocalDateTime? = null,
        dateJoinedTo: LocalDateTime? = null,
        membershipStatus: MembershipStatus? = null
    ): Page<Member> {
        // 필터링을 위한 조건들을 추가하는 QueryBuilder
        val queryBuilder: (Query) -> Query = { query ->
            // option i =  대소문자 구분 없는 이메일 검색
            name?.let { query.addCriteria(Criteria.where("member_name").regex(it, "i")) }
            email?.let { query.addCriteria(Criteria.where("email").regex(it, "i")) }
            dateJoinedFrom?.let { query.addCriteria(Criteria.where("date_joined").gte(it)) }
            dateJoinedTo?.let { query.addCriteria(Criteria.where("date_joined").lte(it)) }
            membershipStatus?.let { query.addCriteria(Criteria.where("membership_status").`is`(it)) }
            query
        }

        return applyPagination(
            pageable = pageable,
            contentQuery = { query ->
                val finalQuery = queryBuilder(query).with(pageable)
                mongoTemplate.find(finalQuery, documentClass)
            },
            countQuery = { query ->
                val finalQuery = queryBuilder(query)
                mongoTemplate.count(finalQuery, documentClass)
            }
        )
    }
}