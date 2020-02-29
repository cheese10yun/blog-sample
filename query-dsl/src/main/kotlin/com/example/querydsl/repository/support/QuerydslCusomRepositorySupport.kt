package com.example.querydsl.repository.support

import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Expression
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import kotlin.properties.Delegates

abstract class QuerydslCusomRepositorySupport(domainClass: Class<*>) : QuerydslRepositorySupport(domainClass) {

    private var queryFactory: JPAQueryFactory by Delegates.notNull()

    @PersistenceContext
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
        this.queryFactory = JPAQueryFactory(entityManager)
    }

    protected fun <T> select(expr: Expression<T>): JPAQuery<T> {
        return queryFactory.select(expr)
    }

    protected fun <T> selectFrom(from: EntityPath<T>): JPAQuery<T> {
        return queryFactory.selectFrom(from)
    }

}