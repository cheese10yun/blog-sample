package com.batch.task.support.reader

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import java.util.function.Function
import javax.persistence.EntityManagerFactory

open class QuerydslNoOffsetPagingItemReader<T>(
    entityManagerFactory: EntityManagerFactory,
    queryFunction: Function<JPAQueryFactory, JPAQuery<T>>,
    jpaProperties: HashMap<String, Any> = hashMapOf(),
    pageSize: Int,
) : QuerydslPagingItemReader<T>(
    entityManagerFactory = entityManagerFactory,
    queryFunction = queryFunction,
    jpaProperties = jpaProperties,
    pageSize = pageSize
) {
    override fun doReadPage() {
        val transaction = getTransactionAndPersistContextFlushAndClear()
    }


    override fun createQuery(): JPAQuery<T> {
        return super.createQuery()
    }
}

