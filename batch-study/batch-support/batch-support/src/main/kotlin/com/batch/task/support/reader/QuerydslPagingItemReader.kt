package com.batch.task.support.reader

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Function
import javax.persistence.EntityManagerFactory
import javax.persistence.EntityTransaction
import org.springframework.batch.item.database.AbstractPagingItemReader
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.util.CollectionUtils

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
        setName("QuerydslPagingItemReader")
    }

    override fun doReadPage() {
        val transaction = getTransactionAndPersistContextFlushAndClear()

        val query = createQuery()
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

    protected open fun createQuery(): JPAQuery<T> = queryFunction.apply(JPAQueryFactory(entityManager))

    /**
     * 영속성 컨텍스트의 flush and clear를 작업을하고 트랜잭션을 begin을 하고 해당 트랜잭션을 리턴한다.
     * @return [EntityTransaction]
     */
    protected fun getTransactionAndPersistContextFlushAndClear(): EntityTransaction {
        val transaction = entityManager.transaction ?: throw DataAccessResourceFailureException("Unable to obtain an EntityManager")
        transaction.begin()
        entityManager.flush()
        entityManager.clear()
        return transaction
    }
}