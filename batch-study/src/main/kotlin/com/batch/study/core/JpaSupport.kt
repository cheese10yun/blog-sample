package com.batch.study.core

import org.springframework.stereotype.Component
import javax.persistence.EntityManager
import javax.transaction.Transactional

/**
 * JpaRepositroy 없이 entityManager를 조작을 쉽게 하는 서포트 클래스
 */
@Component
class JpaSupport(
    private val entityManager: EntityManager
) {

    @Transactional
    fun <T> save(entity: T): T {
        entityManager.persist(entity)
        flushAndClearPersistentContext()
        return entity
    }

    @Transactional
    fun <T> saveAll(entities: Iterable<T>): Iterable<T> {
        for (entity in entities) {
            entityManager.persist(entity)
        }
        flushAndClearPersistentContext()
        return entities
    }

    private fun flushAndClearPersistentContext() {
        entityManager.flush()
        entityManager.clear()
    }
}