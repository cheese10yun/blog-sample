package com.batch.task.support.reader

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.batch.item.database.AbstractPagingItemReader
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.util.CollectionUtils
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Function
import javax.persistence.EntityManagerFactory

open class QuerydslNoOffsetPagingItemReader<T>

/**
 * https://jojoldu.tistory.com/473 참고
 * QueryDsl 기반 PagingItemReader
 */
open class QuerydslPagingItemReader<T> private constructor(
    private val entityManagerFactory: EntityManagerFactory,
    private val queryFunction: Function<JPAQueryFactory, JPAQuery<T>>,
    private val jpaProperties: HashMap<String, Any> = hashMapOf(),
) : AbstractPagingItemReader<T>() {
    private val entityManager by lazy { entityManagerFactory.createEntityManager(jpaProperties) }

    constructor(
        entityManagerFactory: EntityManagerFactory,
        pageSize: Int,
        jpaProperties: HashMap<String, Any> = hashMapOf(),
        queryFunction: Function<JPAQueryFactory, JPAQuery<T>>,
    ) : this(
        entityManagerFactory = entityManagerFactory,
        queryFunction = queryFunction,
        jpaProperties = jpaProperties
    ) {
        setPageSize(pageSize)
    }

    override fun doReadPage() {
        val transaction = entityManager.transaction ?: throw DataAccessResourceFailureException("Unable to obtain an EntityManager")
        transaction.begin()
        entityManager.flush()
        entityManager.clear()

        val jpaQueryFactory = JPAQueryFactory(entityManager)
        val query = queryFunction.apply(jpaQueryFactory)
            .offset((page * pageSize).toLong())
            .limit(pageSize.toLong())
        when {
            CollectionUtils.isEmpty(results) -> results = CopyOnWriteArrayList()
            else -> results.clear()
        }
        results.addAll(query.fetch())
        transaction.commit()
    }

    override fun doJumpToPage(itemIndex: Int) {}
}