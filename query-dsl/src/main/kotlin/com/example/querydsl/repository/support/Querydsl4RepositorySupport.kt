package com.example.querydsl.repository.support

import com.example.querydsl.logger
import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import kotlin.properties.Delegates.notNull
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport
import org.springframework.data.jpa.repository.support.Querydsl
import org.springframework.data.querydsl.SimpleEntityPathResolver
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import java.util.function.Function
import javax.persistence.EntityManager

@Repository
abstract class Querydsl4RepositorySupport(private val domainClass: Class<*>) {

    private val log by logger()

    protected var querydsl: Querydsl by notNull()

    private var queryFactory: JPAQueryFactory by notNull()

    @Autowired
    fun setEntityManager(entityManager: EntityManager) {
        val entityInformation = JpaEntityInformationSupport.getEntityInformation(domainClass, entityManager)
        val resolver = SimpleEntityPathResolver.INSTANCE
        val path = resolver.createPath(entityInformation.javaType)

        this.querydsl = Querydsl(entityManager, PathBuilder(path.type, path.metadata))
        this.queryFactory = JPAQueryFactory(entityManager)
    }


    protected fun <T> select(expr: Expression<T>): JPAQuery<T> {
        return queryFactory.select(expr)
    }

    protected fun <T> selectFrom(from: EntityPath<T>): JPAQuery<T> {
        return queryFactory.selectFrom(from)
    }

    protected fun from(path: EntityPath<*>): JPAQuery<*> {
        return queryFactory.from(path)
    }

    protected fun <T> applyPagination(
        pageable: Pageable,
        contentQuery: Function<JPAQueryFactory, JPAQuery<T>>
    ): Page<T> {
        val jpaQuery = contentQuery.apply(queryFactory)
        val content: List<T> = querydsl.applyPagination(pageable, jpaQuery).fetch() as List<T>
        return PageableExecutionUtils.getPage(content, pageable) { jpaQuery.fetchCount() }
    }

    protected fun <T> applyPagination(
        pageable: Pageable,
        contentQuery: Function<JPAQueryFactory, JPAQuery<T>>,
        countQuery: Function<JPAQueryFactory, JPAQuery<Long>>
    ): Page<T> = runBlocking {
        log.info("thread applyPagination start: : ${Thread.currentThread()}")
        val jpaContentQuery = contentQuery.apply(queryFactory)
        val content = async {
            log.info("thread content: ${Thread.currentThread()}")
            querydsl.applyPagination(pageable, jpaContentQuery).fetch() as List<T>
        }
        val count = async {
            log.info("thread count: : ${Thread.currentThread()}")
            countQuery.apply(queryFactory).fetchFirst()
        }
        log.info("thread applyPagination end: : ${Thread.currentThread()}")

        PageImpl(content.await(), pageable, count.await())
    }

    protected fun <T> applySlicePagination(
        pageable: Pageable,
        query: Function<JPAQueryFactory, JPAQuery<T>>
    ): Slice<T>  {
        val jpaContentQuery = query.apply(queryFactory)
        val content = querydsl.applyPagination(pageable, jpaContentQuery).fetch()
        val hasNext = content.size >= pageable.pageSize
        return SliceImpl(content, pageable, hasNext)
    }
}