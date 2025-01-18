package com.example.mongostudy.mongo

import com.example.mongostudy.logger
import com.example.mongostudy.member.Member
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.result.UpdateResult
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import java.util.*

abstract class MongoCustomRepositorySupport<T>(
    protected val documentClass: Class<T>,
    protected val mongoTemplate: MongoTemplate
) {
    private val logger by logger()

    protected fun logQuery(
        query: Query,
        name: String? = null,
    ) {
        if (logger.isDebugEnabled) {
            logger.debug("Executing MongoDB $name Query: $query")
        }
    }

    protected fun <S : T> applyPagination(
        pageable: Pageable,
        contentQuery: (Query) -> List<S>,
        countQuery: (Query) -> Long
    ) = runBlocking {
        val content = async { contentQuery(Query().with(pageable)) }
        val totalCount = async { countQuery(Query()) }
        PageImpl(content.await(), pageable, totalCount.await())
    }

    protected fun <S : T> applySlicePagination(
        pageable: Pageable,
        contentQuery: (Query) -> List<S>
    ): Slice<S> {
        val content = contentQuery(Query().with(pageable))
        val hasNext = content.size >= pageable.pageSize
        return SliceImpl(content, pageable, hasNext)
    }

    protected fun updateOne(criteria: Criteria, update: Update): UpdateResult {
        val query = Query(criteria)
        return mongoTemplate.updateFirst(query, update, documentClass)
    }

    protected fun updateOne(
        queryProvider: (Query) -> Query,
        updateProvider: (Update) -> Update
    ): UpdateResult {
        return mongoTemplate.updateFirst(queryProvider(Query()), updateProvider(Update()), documentClass)
    }

    protected fun bulkUpdate(
        operations: List<Pair<() -> Query, () -> Update>>, // Query와 Update 생성자를 위한 람다 리스트
        bulkMode: BulkOperations.BulkMode = BulkOperations.BulkMode.UNORDERED,
    ): BulkWriteResult {
        // BulkOperations 객체를 생성합니다.
        val bulkOps = mongoTemplate.bulkOps(bulkMode, documentClass)
        // 제공된 리스트를 반복하면서 bulk 연산에 각 update를 추가합니다.
        operations.forEach { (queryCreator, updateCreator) ->
            bulkOps.updateOne(queryCreator.invoke(), updateCreator.invoke())
        }
        // 모든 업데이트를 실행합니다.
        return bulkOps.execute()
    }

    protected fun bulkDelete(
        query: Query,
        bulkMode: BulkOperations.BulkMode
    ): BulkWriteResult {
        return mongoTemplate
            .bulkOps(bulkMode, documentClass)
            .remove(query)
            .execute()
    }

    fun deleteByIds(ids: List<ObjectId>) {
        val query = Query(Criteria.where("_id").`in`(ids))
        mongoTemplate.remove(query, documentClass)
    }

//    protected fun bulkUpdate(
//        ids: List<ObjectId>,
//        bulkMode: BulkOperations.BulkMode = BulkOperations.BulkMode.UNORDERED
//    ): BulkWriteResult {
//        val bulkOps = mongoTemplate.bulkOps(bulkMode, Member::class.java)
//        for (id in ids) {
//            bulkOps.updateOne(
//                Query(Criteria.where("_id").`is`(id)),
//                Update().set("name", UUID.randomUUID().toString())
//            )
//        }
//        return bulkOps.execute()
//    }

    protected fun findFirst(queryBuilder: (Query) -> Query): T? {
        val query = queryBuilder(Query())
        return mongoTemplate.findOne(query.limit(1), documentClass)
    }

    /**
     * 여러 문서를 업데이트합니다.
     *
     * @param criteria 업데이트할 문서를 선택하는 데 사용되는 Criteria 객체.
     * @param update 업데이트할 내용을 지정하는 Update 객체.
     * @return 업데이트된 문서 수
     */
    fun updateMany(criteria: Criteria, update: Update): Long {
        val query = Query(criteria)
        val result = mongoTemplate.updateMulti(query, update, documentClass)
        return result.modifiedCount
    }

    /**
     * 여러 도메인 객체를 한 번에 MongoDB에 삽입합니다.
     *
     * @param entities 삽입할 도메인 객체들의 목록.
     */
    fun insertAll(entities: List<T>) {
        mongoTemplate.insertAll(entities)
    }


}
