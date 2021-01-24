package com.batch.study

import org.junit.jupiter.api.TestInstance
import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.JobRepositoryTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor

@SpringBootTest(
    properties = [
        "spring.batch.job.enabled=false", // 테스트 수행 시 컨텍스트가 시작될 때 Job이 실행되는 것을 막기 위한 프로퍼티 설정
        "spring.batch.job.names="
    ]
)
@SpringBatchTest
@ActiveProfiles("test")
@EnableBatchProcessing
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
abstract class BatchApplicationTestSupport : BatchJpaTestSupport() {

    @Autowired
    protected lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @Autowired
    protected lateinit var jobRepositoryTestUtils: JobRepositoryTestUtils

    protected fun launchJob(job: Job) {
        jobLauncherTestUtils.job = job
        jobLauncherTestUtils.launchJob()
    }
}