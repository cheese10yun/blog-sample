package com.example.querydsl.repository.support

import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport
import org.springframework.data.jpa.repository.support.Querydsl
import org.springframework.data.querydsl.SimpleEntityPathResolver
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import java.util.function.Function
import javax.persistence.EntityManager
import kotlin.properties.Delegates.notNull

@Repository
abstract class Querydsl4RepositorySupport(private val domainClass: Class<*>) {

    private var querydsl: Querydsl by notNull()

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

    protected fun <T> applyPagination(pageable: Pageable, contentQuery: Function<JPAQueryFactory, JPAQuery<*>>): Page<T> {
        val jpaQuery = contentQuery.apply(queryFactory)
        val content: List<T> = querydsl.applyPagination(pageable, jpaQuery).fetch() as List<T>
        return PageableExecutionUtils.getPage(content, pageable) { jpaQuery.fetchCount() }
    }

    protected fun applyPagination(pageable: Pageable, contentQuery: Function<JPAQueryFactory, JPAQuery<*>>, countQuery: Function<JPAQueryFactory, JPAQuery<*>>): Page<Any> {
        val jpaContentQuery = contentQuery.apply(queryFactory)
        val content = querydsl.applyPagination(pageable, jpaContentQuery).fetch()
        val countQuery = countQuery.apply(queryFactory)
        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchCount() }
    }
}