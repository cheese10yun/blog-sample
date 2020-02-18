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
    pageSize: Int, // page size == chunk size
    protected val entityManagerFactory: EntityManagerFactory,
    protected val query: (JPAQueryFactory) -> JPAQuery<T>
) : AbstractPagingItemReader<T>() {
    private val jpaPropertyMap = hashMapOf<String, Any>()
    protected lateinit var entityManager: EntityManager
    private var transacted = true

    init {
        super.setName(name)
        super.setPageSize(pageSize)
    }

    override fun doOpen() {
        super.doOpen()

        entityManager = entityManagerFactory.createEntityManager(jpaPropertyMap).let {
            it ?: throw DataAccessResourceFailureException("Unable to obtain an EntityManager")
        }
    }

    override fun doReadPage() {
        clearEntityManagerIfTransacted()

        initResults()

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

    protected fun clearEntityManagerIfTransacted() {
        when {
            this.transacted -> this.entityManager.clear()
        }
    }

    protected open fun createQuery(): JPAQuery<T> {
        return this.query.invoke(JPAQueryFactory(entityManager))
    }

    protected fun JPAQuery<T>.limit(limit: Int): JPAQuery<T> {
        return this.limit(limit.toLong())
    }

    protected fun JPAQuery<T>.offset(offset: Int): JPAQuery<T> {
        return this.offset(offset.toLong())
    }

    protected fun initResults() {
        when {
            super.results.isNullOrEmpty() -> super.results = CopyOnWriteArrayList()
            else -> super.results.clear()
        }
    }
}