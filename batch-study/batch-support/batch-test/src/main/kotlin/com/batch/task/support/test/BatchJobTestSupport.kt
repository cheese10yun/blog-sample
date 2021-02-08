package com.batch.task.support.test

import org.junit.jupiter.api.TestInstance
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import javax.persistence.EntityManager

@SpringBootTest(
    properties = [
        "spring.batch.job.enabled=false", // Job Bean 전체가 올라오는 것을 방지
        "spring.batch.job.names="
    ]
)
@SpringBatchTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
abstract class BatchJobTestSupport {

    @Autowired
    protected lateinit var entityManager: EntityManager

    @Autowired
    protected lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    protected fun launchJob(job: Job, jobParameters: JobParameters = jobLauncherTestUtils.uniqueJobParameters) {
        jobLauncherTestUtils.job = job
        jobLauncherTestUtils.launchJob(jobParameters)
    }

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