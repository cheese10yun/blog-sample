package com.batch.task.support.test

import javax.persistence.EntityManager

abstract class BatchTestJpaSupport(
    private val entityManager: EntityManager
) {

    protected fun <T> save(entity: T): T {
        entityManager.transaction.let {
            it.begin()
            entityManager.persist(entity)
            it.commit()

            entityManager.clear()
        }
        return entity
    }

    protected fun <T> saveAll(entities: List<T>): List<T> {
        entityManager.transaction.let {
            it.begin()
            entities.let { entityManager.persist(it) }
            it.commit()
            entityManager.clear()
        }
        return entities
    }
}