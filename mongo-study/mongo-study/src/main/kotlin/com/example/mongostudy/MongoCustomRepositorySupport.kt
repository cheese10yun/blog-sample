package com.example.mongostudy

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query

abstract class MongoCustomRepositorySupport<T>(
    protected val domainClass: Class<T>,
    protected val mongoTemplate: MongoTemplate
) {

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

}