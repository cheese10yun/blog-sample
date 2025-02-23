package com.example.boot3mongo

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
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.sort
import org.springframework.data.mongodb.core.aggregation.AggregationResults
import org.springframework.data.mongodb.core.aggregation.SortOperation
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

    protected fun <S> applyPaginationAggregation(
        pageable: Pageable,
        contentAggregation: Aggregation,
        countAggregation: Aggregation,
        contentQuery: (Aggregation) -> AggregationResults<S>,
        countQuery: (Aggregation) -> AggregationResults<MongoCount>
    ): PageImpl<S> = runBlocking {
        addAggregationPageAndSort(pageable, contentAggregation)

        countAggregation.pipeline.apply {
            this.add(Aggregation.count().`as`("count"))
        }

        // Perform queries asynchronously
        val contentDeferred = async { contentQuery(contentAggregation) }
        val countDeferred = async { countQuery(countAggregation) }

        val content = contentDeferred.await().mappedResults
        val totalCount = countDeferred.await().uniqueMappedResult?.count ?: 0L

        PageImpl(content, pageable, totalCount)
    }

    protected fun <S : T> applySlice(
        pageable: Pageable,
        contentQuery: (Query) -> List<S>
    ): Slice<S> {
        val content = contentQuery(Query().with(pageable))
        val hasNext = content.size >= pageable.pageSize
        return SliceImpl(content, pageable, hasNext)
    }

    protected fun <S> applySliceAggregation(
        pageable: Pageable,
        contentAggregation: Aggregation,
        contentQuery: (Aggregation) -> AggregationResults<S>
    ): Slice<S> {
        addAggregationPageAndSort(pageable, contentAggregation)
        val results = contentQuery(contentAggregation)
        val content = results.mappedResults
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
    protected fun updateMany(criteria: Criteria, update: Update): Long {
        val query = Query(criteria)
        val result = mongoTemplate.updateMulti(query, update, documentClass)
        return result.modifiedCount
    }

    /**
     * 여러 도메인 객체를 한 번에 MongoDB에 삽입합니다.
     *
     * @param entities 삽입할 도메인 객체들의 목록.
     */
    protected fun insertAll(entities: List<T>) {
        mongoTemplate.insertAll(entities)
    }

    private fun addAggregationPageAndSort(pageable: Pageable, aggregation: Aggregation) {
        val hasSort = hasSortOperation(aggregation)
        aggregation.pipeline.apply {
            if (hasSort.not() && pageable.sort.isEmpty.not()) {
                this.add(sort(pageable.sort))
            }
            this.add(Aggregation.skip((pageable.pageNumber * pageable.pageSize).toLong()))
            this.add(Aggregation.limit(pageable.pageSize.toLong()))
        }
    }

    private fun hasSortOperation(aggregation: Aggregation) = aggregation.pipeline.operations.any { it is SortOperation }
}

data class MongoCount(
    val count: Long
)