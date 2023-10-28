package com.example.mongostudy.member

import com.example.mongostudy.mongo.MongoCustomRepositorySupport
import com.example.mongostudy.mongo.eqIfNotNull
import com.example.mongostudy.mongo.fieldName
import java.math.BigDecimal
import java.time.LocalDateTime
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.mapping.div
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface MemberRepository : MongoRepository<Member, ObjectId>, MemberCustomRepository, QuerydslPredicateExecutor<Member>

interface MemberCustomRepository {
    fun findByName(name: String): List<Member>
    fun findByEmail(email: String): List<Member>
    fun findActiveMembers(): List<Member>
    fun findMembersWithPointsOver(points: BigDecimal): List<Member>
    fun findPageBy(
        pageable: Pageable,
        name: String?,
        email: String?,
        dateJoinedFrom: LocalDateTime?,
        dateJoinedTo: LocalDateTime?,
        memberStatus: MemberStatus?
    ): Page<Member>

    fun findSlice(
        pageable: Pageable,
        name: String?,
        email: String?
    ): Slice<Member>
}

class MemberCustomRepositoryImpl(mongoTemplate: MongoTemplate) : MemberCustomRepository, MongoCustomRepositorySupport<Member>(
    Member::class.java,
    mongoTemplate
) {
    override fun findByName(name: String): List<Member> {
        val query = Query(Criteria().eqIfNotNull(Member::address / Address::addressDetail, 123))
        return mongoTemplate.find(query, Member::class.java)
    }

    override fun findByEmail(email: String): List<Member> {
        val query = Query(Criteria.where("email").`is`(email))
        return mongoTemplate.find(query, Member::class.java)
    }

    override fun findActiveMembers(): List<Member> {
        val query = Query(Criteria.where(Member::status.fieldName()).`is`(MemberStatus.ACTIVE))
        return mongoTemplate.find(query, Member::class.java)
    }

    override fun findMembersWithPointsOver(points: BigDecimal): List<Member> {
        val query = Query(Criteria.where(Member::pointsAccumulated.fieldName()).gt(points))
        return mongoTemplate.find(query, Member::class.java)
    }

    override fun findPageBy(
        pageable: Pageable,
        name: String?,
        email: String?,
        dateJoinedFrom: LocalDateTime?,
        dateJoinedTo: LocalDateTime?,
        memberStatus: MemberStatus?
    ): Page<Member> {

        val queryBuilder: (Query) -> Query = { query ->
            val criteria = Criteria().apply {
                name?.let { and(Member::name.fieldName()).regex(".*$it.*", "i") }
                email?.let { and(Member::email.fieldName()).regex(".*$it.*", "i") }
                dateJoinedFrom?.let { and(Member::dateJoined.fieldName()).gte(it) }
                dateJoinedTo?.let { and(Member::dateJoined.fieldName()).lte(it) }
                memberStatus?.let { and(Member::status.fieldName()).`is`(it) }
            }
            query.addCriteria(criteria)
        }

        return applyPagination(
            pageable = pageable,
            contentQuery = { mongoTemplate.find(queryBuilder(it), documentClass) },
            countQuery = { mongoTemplate.count(queryBuilder(it), documentClass) }
        )
    }

    override fun findSlice(
        pageable: Pageable,
        name: String?,
        email: String?
    ): Slice<Member> {
        val queryBuilder: (Query) -> Query = { query ->
            val criteria = Criteria().apply {
                name?.let { and(Member::name.fieldName()).regex(".*$it.*", "i") }
                email?.let { and(Member::email.fieldName()).regex(".*$it.*", "i") }
            }
            query.addCriteria(criteria)
        }
        return applySlicePagination(
            pageable = pageable,
            contentQuery = { mongoTemplate.find(queryBuilder(it), documentClass) }
        )
    }
}