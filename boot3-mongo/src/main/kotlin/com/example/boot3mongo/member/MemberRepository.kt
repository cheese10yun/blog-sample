package com.example.boot3mongo.member

import com.example.boot3mongo.MongoCount
import com.example.boot3mongo.MongoCustomRepositorySupport
import com.example.boot3mongo.mongo.eqIfNotNull
import com.example.boot3mongo.mongo.gtIfNotNull
import com.mongodb.client.result.UpdateResult
import java.math.BigDecimal
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.mapping.div
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.find

interface MemberRepository : MongoRepository<Member, ObjectId>, MemberCustomRepository

interface MemberCustomRepository {
    // find
    fun findByName(name: String): List<Member>
    fun findBy(addressDetail: String): List<Member>
    fun findByEmail(email: String): List<Member>
    fun findActiveMembers(): List<Member>
    fun findMembersWithPointsOver(points: BigDecimal): List<Member>
    fun findPage(pageable: Pageable, name: String?, email: String?, memberId: String?): Page<Member>
    fun findPageAggregation(pageable: Pageable, name: String?, email: String?, memberId: String?): Page<MemberProjection>
    fun findSlice(pageable: Pageable, name: String?, email: String?, memberId: String?): Slice<Member>
    fun findSliceAggregation(pageable: Pageable, name: String?, email: String?, memberId: String?): Slice<MemberProjection>

    // update
    fun updateName(targets: List<MemberQueryForm.UpdateName>)
    fun update(id: ObjectId): UpdateResult
    fun updateFirst(id: ObjectId): UpdateResult

    // insert
    fun insertMany(members: List<Member>)
}

class MemberCustomRepositoryImpl(mongoTemplate: MongoTemplate) : MemberCustomRepository, MongoCustomRepositorySupport<Member>(Member::class.java, mongoTemplate) {

    override fun findByName(name: String): List<Member> {
        val query = Query(Criteria().eqIfNotNull(Member::name, name))
        return mongoTemplate.find<Member>(query)
    }

    override fun findBy(addressDetail: String): List<Member> {
        val query = Query(Criteria().eqIfNotNull(Member::address / Address::addressDetail, 123))
        return mongoTemplate.find<Member>(query)
    }

    override fun findByEmail(email: String): List<Member> {
        val query = Query(Criteria().eqIfNotNull(Member::email, email))
        return mongoTemplate.find<Member>(query)
    }

    override fun findActiveMembers(): List<Member> {
        val query = Query(Criteria().eqIfNotNull(Member::status, MemberStatus.ACTIVE))
        return mongoTemplate.find<Member>(query)
    }

    override fun findMembersWithPointsOver(points: BigDecimal): List<Member> {
        val query = Query(Criteria().gtIfNotNull(Member::pointsAccumulated, points))
        return mongoTemplate.find<Member>(query)
    }

    override fun findPage(
        pageable: Pageable,
        name: String?,
        email: String?,
        memberId: String?
    ): Page<Member> {
        val criteria = Criteria().apply {
            name?.let { this.and("name").`is`(it) }
            email?.let { this.and("email").`is`(it) }
            memberId?.let { this.and("member_id").`is`(it) }
        }

        return applyPagination(
            pageable = pageable,
            contentQuery = { mongoTemplate.find<Member>(it.addCriteria(criteria)) },
            countQuery = { mongoTemplate.count(it.addCriteria(criteria), documentClass) }
        )
    }

    override fun findPageAggregation(
        pageable: Pageable,
        name: String?,
        email: String?,
        memberId: String?
    ): Page<MemberProjection> {
        val match = match(
            Criteria().apply {
                name?.let { this.and("name").`is`(it) }
                email?.let { this.and("email").`is`(it) }
                memberId?.let { this.and("member_id").`is`(it) }
            }
        )
        val projection = project()
            .andInclude("name")
            .andInclude("email")

        val contentAggregation = newAggregation(
            match,
            projection,
        )

        return applyPaginationAggregation(
            pageable = pageable,
            contentAggregation = contentAggregation,
            countAggregation = newAggregation(match),
            contentQuery = {
                mongoTemplate.aggregate(it, Member.DOCUMENT_NAME, MemberProjection::class.java)
            },
            countQuery = {
                mongoTemplate.aggregate(it, Member.DOCUMENT_NAME, MongoCount::class.java)
            }
        )

    }

    override fun findSlice(
        pageable: Pageable,
        name: String?,
        email: String?,
        memberId: String?
    ): Slice<Member> {
        val criteria = Criteria()
            .apply {
                name?.let { this.and("name").`is`(it) }
                email?.let { this.and("email").`is`(it) }
                memberId?.let { this.and("member_id").`is`(it) }
            }

        return applySlice(
            pageable = pageable,
            contentQuery = {
                mongoTemplate.find<Member>(it.addCriteria(criteria))
            }
        )
    }

    override fun findSliceAggregation(
        pageable: Pageable,
        name: String?,
        email: String?,
        memberId: String?
    ): Slice<MemberProjection> {
        val match = match(
            Criteria().apply {
                name?.let { this.and("name").`is`(it) }
                email?.let { this.and("email").`is`(it) }
                memberId?.let { this.and("member_id").`is`(it) }
            }
        )
        val projection = project()
            .andInclude("name")
            .andInclude("email")

        val contentAggregation = newAggregation(
            match,
            projection,
        )
        return this.applySliceAggregation(
            pageable = pageable,
            contentAggregation = contentAggregation,
            contentQuery = {
                mongoTemplate.aggregate(it, Member.DOCUMENT_NAME, MemberProjection::class.java)
            }
        )
    }

    override fun updateName(targets: List<MemberQueryForm.UpdateName>) {
        bulkUpdate(
            targets.map {
                Pair(
                    { Query(Criteria.where("id").`is`(it.id)) },
                    { Update().set("name", it.name) }
                )
            }
        )
    }

    override fun insertMany(members: List<Member>) {
        insertAll(members)
    }

    override fun update(id: ObjectId): UpdateResult {
        return mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(id)),
            Update().set("name", UUID.randomUUID().toString()),
            documentClass
        )
    }

    override fun updateFirst(id: ObjectId): UpdateResult {
        return mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(id)),
            Update().set("name", UUID.randomUUID().toString()),
            Member::class.java
        )
    }
}

data class MemberProjection(
    val name: String,
    val email: String,
)