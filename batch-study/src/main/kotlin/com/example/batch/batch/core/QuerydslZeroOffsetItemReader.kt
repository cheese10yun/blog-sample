package com.example.batch.batch.core


import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.NumberPath
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Sort
import javax.persistence.EntityManagerFactory
import kotlin.properties.Delegates

open class QuerydslZeroOffsetItemReader<T : Any>(
    name: String,
    pageSize: Int,
    entityManagerFactory: EntityManagerFactory,
    private val expression: Expression,
    private val id: NumberPath<Long>,
    query: (JPAQueryFactory) -> JPAQuery<T>
) : QuerydslPagingItemReader<T>(
    name,
    pageSize,
    entityManagerFactory,
    query
) {

    private var offsetId: Long by Delegates.notNull()

    private var lastId: Long by Delegates.notNull()

    private val variationOfOffset: Int = if (expression.isAsc) pageSize else -1

    override fun doOpen() {

        fun findFirstId(): Long =
            query.invoke(JPAQueryFactory(entityManager))
                .select(if (expression.isAsc) id.min() else id.max())
                .fetchOne() ?: 0

        fun findLastId(): Long =
            query.invoke(JPAQueryFactory(entityManager))
                .select(if (expression.isAsc) id.max() else id.min())
                .fetchOne() ?: -1

        super.doOpen()

        offsetId = findFirstId()
        lastId = findLastId()
    }

    override fun doReadPage() {

        fun hasNextChunk(): Boolean =
            results.isEmpty() && if (expression.isAsc) offsetId <= lastId else offsetId > -1

        clearEntityManagerIfTransacted()

        initResults()

        while (hasNextChunk()) {
            createQuery().fetchQuery()

            offsetId += variationOfOffset
        }
    }

    override fun createQuery(): JPAQuery<T> {
        return query.invoke(JPAQueryFactory(entityManager))
            .where(expression.where(id, offsetId, pageSize))
            .orderBy(expression.order(id))
            .limit(pageSize.toLong())
    }

}

object Asc : Expression(
    { id, offsetId, pageSize ->
        id.goe(offsetId).and(id.lt(offsetId + pageSize))
    },
    Sort.Direction.ASC
)

object Desc : Expression(
    { id, offsetId, pageSize ->
        id.gt(offsetId - pageSize).and(id.loe(offsetId))
    },
    Sort.Direction.DESC
)

open class Expression(
    private val whereExpression: (id: NumberPath<Long>, offsetId: Long, pageSize: Int) -> BooleanExpression,
    private val direction: Sort.Direction
) {
    val isAsc: Boolean get() = direction == Sort.Direction.ASC

    fun where(id: NumberPath<Long>, offsetId: Long, pageSize: Int): BooleanExpression = whereExpression.invoke(id, offsetId, pageSize)

    fun order(id: NumberPath<Long>): OrderSpecifier<Long> = if (isAsc) id.asc() else id.desc()
}