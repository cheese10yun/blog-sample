package com.example.batch.batch.core

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.batch.item.database.AbstractPagingItemReader
import org.springframework.dao.DataAccessResourceFailureException
import java.util.concurrent.CopyOnWriteArrayList
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory

open class QuerydslPagingItemReader<T>(
    name: String,
    pageSize: Int,
    protected val entityManagerFactory: EntityManagerFactory,
    protected val query: (JPAQueryFactory) -> JPAQuery<T>
) : AbstractPagingItemReader<T>() {
    protected val jpaPropertyMap = hashMapOf<String, Any>()
    protected var transacted: Boolean = true

    protected lateinit var entityManager: EntityManager

    init {
        super.setName(name)
        super.setPageSize(pageSize)
    }

    override fun doOpen() {
        super.doOpen()

        entityManager = entityManagerFactory.createEntityManager(jpaPropertyMap).let {
            if (it == null) {
                throw DataAccessResourceFailureException("EntityManager를 가져오지 못했습니다.")
            }
            it
        }
    }

    protected fun initResult() {
        if (results == null || results.isEmpty()) {
            results = CopyOnWriteArrayList()
        } else {
            results.clear()
        }
    }

    protected fun clearTransactionManagerIfTransacted() {
        if (transacted) {
            entityManager.clear()
        }
    }

    protected open fun createQuery(): JPAQuery<T> = query.invoke(JPAQueryFactory(entityManager))

    override fun doReadPage() {
        clearTransactionManagerIfTransacted()

        initResult()

        createQuery()
            .offset(page * pageSize)
            .limit(pageSize)
            .fetchQuery()
    }

    override fun doJumpToPage(itemIndex: Int) {}

    override fun doClose() {
        entityManager.close()
        super.doClose()
    }

    protected fun JPAQuery<T>.limit(limit: Int): JPAQuery<T> = this.limit(limit.toLong())

    protected fun JPAQuery<T>.offset(offset: Int): JPAQuery<T> = this.offset(offset.toLong())

    protected fun JPAQuery<T>.fetchQuery() {
        this.fetch().let { queryResult ->
            if (transacted) {
                results.addAll(queryResult)
            } else {
                queryResult.forEach { item ->
                    entityManager.detach(item)
                    results.add(item)
                }
            }
        }
    }
}