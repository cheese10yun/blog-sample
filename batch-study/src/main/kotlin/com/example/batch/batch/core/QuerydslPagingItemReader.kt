package com.example.batch.batch.core

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.batch.item.database.AbstractPagingItemReader
import org.springframework.dao.DataAccessResourceFailureException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Function
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory

open class QuerydslPagingItemReader<T>(
    name: String,
    pageSize: Int, // page size == chunk size
    protected val entityManagerFactory: EntityManagerFactory,
    protected val queryFunction: Function<JPAQueryFactory, JPAQuery<T>>

) : AbstractPagingItemReader<T>() {
    private val jpaPropertyMap = hashMapOf<String, Any>()
    private lateinit var entityManager: EntityManager
    private var transacted = true

    init {
        super.setName(name)
        super.setPageSize(pageSize)
    }

    override fun doOpen() {
        entityManager = entityManagerFactory.createEntityManager(jpaPropertyMap).let {
            it ?: throw DataAccessResourceFailureException("Unable to obtain an EntityManager")
        }
    }

    override fun doReadPage() {
        clearEntityManagerIfTransacted()
        val query = createQuery()
            .offset((page * pageSize).toLong())
            .limit(pageSize.toLong())
        initResults()
        fetchQuery(query)
    }

    override fun doJumpToPage(itemIndex: Int) {}

    override fun doClose() {
        entityManager.close()
        super.doClose()
    }

    private fun fetchQuery(query: JPAQuery<T>) {
        when {
            this.transacted.not() -> {
                val results = query.fetch()
                for (entity in results) {
                    this.entityManager.detach(entity)
                    results.add(entity)
                }
            }
            else -> super.results.addAll(query.fetch())
        }
    }


    private fun clearEntityManagerIfTransacted() {
        when {
            this.transacted -> this.entityManager.clear()
        }
    }

    private fun createQuery(): JPAQuery<T> {
        return this.queryFunction.apply(JPAQueryFactory(this.entityManager))
    }

    private fun initResults() {
        when {
            super.results.isNullOrEmpty() -> super.results = CopyOnWriteArrayList()
            else -> super.results.clear()
        }
    }
}