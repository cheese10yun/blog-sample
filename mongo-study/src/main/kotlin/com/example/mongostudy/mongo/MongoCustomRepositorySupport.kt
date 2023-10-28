package com.example.mongostudy.mongo

import com.example.mongostudy.logger
import com.mongodb.client.result.UpdateResult
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

abstract class MongoCustomRepositorySupport<T>(
    protected val documentClass: Class<T>,
    protected val mongoTemplate: MongoTemplate
) {
    private val logger by logger()

    protected fun logQuery(
        query: Query,
        name: String?= null,
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
        val queryForContent = Query().with(pageable)
        val content = contentQuery(queryForContent)
        val hasNext = content.size > pageable.pageSize

        return SliceImpl(content.take(pageable.pageSize), pageable, hasNext)
    }

    protected fun updateOne(criteria: Criteria, update: Update): UpdateResult {
        val query = Query(criteria)
        return mongoTemplate.updateFirst(query, update, documentClass)
    }

    protected fun updateOne(
        queryProvider: (Query) -> Query,
        updateProvider: (Update) -> Update
    ): UpdateResult {
        val queryProvider1 = queryProvider(Query())
        val updateProvider1 = updateProvider(Update())
        return mongoTemplate.updateFirst(queryProvider1, updateProvider1, documentClass)
    }

    fun findFirst(queryBuilder: (Query) -> Query): T? {
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
    fun insertMany(entities: List<T>) {
        mongoTemplate.insertAll(entities)
    }


}
