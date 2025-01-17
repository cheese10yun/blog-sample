package com.example.mongostudy.member

import com.example.mongostudy.mongo.MongoCustomRepositorySupport
import com.example.mongostudy.mongo.eqIfNotNull
import com.example.mongostudy.mongo.gtIfNotNull
import com.example.mongostudy.mongo.gteIfNotNull
import com.example.mongostudy.mongo.lteIfNotNull
import com.mongodb.client.result.UpdateResult
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
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface MemberRepository : MongoRepository<Member, ObjectId>, MemberCustomRepository

interface MemberCustomRepository {
    // find
    fun findByName(name: String): List<Member>
    fun findBy(addressDetail: String): List<Member>
    fun findByEmail(email: String): List<Member>
    fun findActiveMembers(): List<Member>
    fun findMembersWithPointsOver(points: BigDecimal): List<Member>
    fun findPageBy(pageable: Pageable, name: String?, email: String?, dateJoinedFrom: LocalDateTime?, dateJoinedTo: LocalDateTime?, memberStatus: MemberStatus?): Page<Member>
    fun findSlice(pageable: Pageable, name: String?, email: String?): Slice<Member>

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
        return mongoTemplate.find(query, documentClass)
    }

    override fun findBy(addressDetail: String): List<Member> {
        val query = Query(Criteria().eqIfNotNull(Member::address / Address::addressDetail, 123))
        return mongoTemplate.find(query, documentClass)
    }

    override fun findByEmail(email: String): List<Member> {
        val query = Query(Criteria().eqIfNotNull(Member::email, email))
        return mongoTemplate.find(query, documentClass)
    }

    override fun findActiveMembers(): List<Member> {
        val query = Query(Criteria().eqIfNotNull(Member::status, MemberStatus.ACTIVE))
        return mongoTemplate.find(query, documentClass)
    }

    override fun findMembersWithPointsOver(points: BigDecimal): List<Member> {
        val query = Query(Criteria().gtIfNotNull(Member::pointsAccumulated, points))

        return mongoTemplate.find(query, documentClass)
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
            query.addCriteria(
                Criteria()
                    .eqIfNotNull(Member::name, name)
                    .eqIfNotNull(Member::email, email)
                    .gteIfNotNull(Member::dateJoined, dateJoinedFrom)
                    .lteIfNotNull(Member::dateJoined, dateJoinedTo)
                    .eqIfNotNull(Member::status, memberStatus)
            )
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
            val criteria = Criteria()
                .eqIfNotNull(Member::name, name)
                .eqIfNotNull(Member::email, email)

            query.addCriteria(criteria)
        }
        return applySlicePagination(
            pageable = pageable,
            contentQuery = { mongoTemplate.find(queryBuilder(it), documentClass) }
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