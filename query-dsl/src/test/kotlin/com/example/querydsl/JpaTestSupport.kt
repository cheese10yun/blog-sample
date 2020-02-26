package com.example.querydsl

import org.springframework.boot.test.context.TestConfiguration
import javax.persistence.EntityManager

@TestConfiguration
class JpaTestSupport(
    private val entityManager: EntityManager
) {

    protected fun <T> save(entity: T): T {
        entityManager.persist(entity)

        entityManager.flush()
        entityManager.clear()

        return entity
    }

    protected fun <T> saveAll(entities: Iterable<T>): Iterable<T> {

        for (entity in entities) {
            entityManager.persist(entity)
        }

        return entities
    }
}