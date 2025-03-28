package com.example.mongostudy.member

import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class MemberQueryService(
    private val memberRepository: MemberRepository
) {

    fun findPageBy(
        pageable: Pageable,
        name: String?,
        email: String?,
        memberId: String?
    ): Page<Member> {
        return memberRepository.findPage(
            pageable = pageable,
            name = name,
            email = email,
            memberId = memberId,
        )
    }

    fun updateBulkTest(pairs: List<Pair<() -> Query, () -> Update>>, bulkMode: BulkOperations.BulkMode) {
        val pair = listOf(
            Pair(
                first = { Query(Criteria.where("_id").`is`(ObjectId("id"))) },
                second = { Update().set("name", UUID.randomUUID().toString()) }
            )
        )
//        memberRepository.updateNmae(pair, BulkOperations.BulkMode.UNORDERED)
    }

    fun update() {
    }

    fun update(members: List<Member>) {
        memberRepository.saveAll(members)
    }
}