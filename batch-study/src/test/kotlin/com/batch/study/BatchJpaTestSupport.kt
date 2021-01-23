package com.batch.study

import org.springframework.beans.factory.annotation.Autowired
import javax.persistence.EntityManager
import javax.transaction.Transactional

class BatchJpaTestSupport : BatchApplicationTestSupport() {

    @Autowired
    protected lateinit var entityManager: EntityManager

    @Transactional
    protected fun <T> save(entity: T): T {
        entityManager.persist(entity)
        flushAndClearPersistentContext()
        return entity
    }

    @Transactional
    protected fun <T> saveAll(entities: Iterable<T>): Iterable<T> {
        for (entity in entities) {
            entityManager.persist(entity)
        }
        flushAndClearPersistentContext()
        return entities
    }

    protected fun <T> saveAll(entities: List<T>): List<T> = saveAll(entities.asIterable()).toList()

    protected fun <T> List<T>.persistAll(): List<T> = saveAll(this)

    private fun flushAndClearPersistentContext() {
        entityManager.flush()
        entityManager.clear()
    }

}