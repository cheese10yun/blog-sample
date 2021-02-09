package com.batch.task.support.test

import com.querydsl.core.types.EntityPath
import com.querydsl.jpa.impl.JPAQueryFactory
import org.junit.jupiter.api.TestInstance
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import javax.persistence.EntityManagerFactory

@SpringBootTest(
    properties = [
        "spring.batch.job.enabled=false", // Job Bean 전체가 올라오는 것을 방지
        "spring.batch.job.names="
    ]
)
@SpringBatchTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles("test")
abstract class BatchJobTestSupport {

    @Autowired
    protected lateinit var entityManagerFactory: EntityManagerFactory
    protected val entityManager by lazy { entityManagerFactory.createEntityManager() }

    @Autowired
    protected lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    protected val query: JPAQueryFactory by lazy { JPAQueryFactory(entityManager) }

    protected fun launchJob(job: Job, jobParameters: JobParameters = jobLauncherTestUtils.uniqueJobParameters) {
        jobLauncherTestUtils.job = job
        jobLauncherTestUtils.launchJob(jobParameters)
    }

    protected fun <E> List<E>.persist() {
        saveAll(this)
    }

    protected fun <T> save(entity: T): T {
        entityManager.transaction.let { transaction ->
            transaction.begin()
            entityManager.persist(entity)
            transaction.commit()
            entityManager.clear()
        }
        return entity
    }

    protected fun <T> saveAll(entities: List<T>): List<T> {
        val entityManager = entityManagerFactory.createEntityManager()
        entityManager.transaction.let { transaction ->
            transaction.begin()
            for (entity in entities) {
                entityManager.persist(entity)
            }
            transaction.commit()
            entityManager.clear()
        }
        return entities
    }


    protected fun <T> deleteAll(path: EntityPath<T>) {
        entityManager.transaction.let { transaction ->
            transaction.begin()
            query.delete(path).execute()
            transaction.commit()
        }
    }
}