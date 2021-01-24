package com.batch.study

import com.querydsl.core.types.EntityPath
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory

@Configuration
abstract class BatchJpaTestSupport {
    private val log by logger()

    @Autowired
    lateinit var entityManagerFactory: EntityManagerFactory

    private val entityManager: EntityManager by lazy {
        entityManagerFactory.createEntityManager()
    }

    protected val query: JPAQueryFactory by lazy { JPAQueryFactory(entityManager) }

    protected fun <T> save(entity: T): T {
        entityManager.transaction?.let { transaction ->
            transaction.begin()
            try {
                entityManager.persist(entity)
                transaction.commit()
                entityManager.clear()
            } catch (e: Exception) {
                transaction.rollbackOnly
                log.error(e.message, e)
                throw e
            }
        }
        return entity
    }

    protected fun <T> List<T>.persistAll(): List<T> = saveAll(this)

    protected fun <T> saveAll(entities: List<T>): List<T> {
        val results = mutableListOf<T>()
        entityManager.transaction?.let { transaction ->
            transaction.begin()
            try {
                entities.forEach { entity ->
                    entityManager.persist(entity)
                    results.add(entity)
                }
                transaction.commit()
                entityManager.clear()
            } catch (e: Exception) {
                transaction.rollback()
                log.error(e.message, e)
                throw e
            }
        }
        return results
    }

    protected fun <T> deleteAll(path: EntityPath<T>) {
        entityManager.transaction?.let { transaction ->
            transaction.begin()
            try {
                query.delete(path).execute()
                transaction.commit()
            } catch (e: Exception) {
                transaction.rollback()
                log.error(e.message, e)
                throw e
            }
        }
    }
}