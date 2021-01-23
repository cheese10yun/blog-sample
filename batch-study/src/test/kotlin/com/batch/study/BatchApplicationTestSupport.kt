package com.batch.study

import org.junit.jupiter.api.TestInstance
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.JobRepositoryTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor

@SpringBootTest(
    properties = [
        "spring.batch.job.enabled=false",
        "spring.batch.job.names=readerPerformanceJob"
    ]
)
@SpringBatchTest
@ActiveProfiles("test")
@EnableBatchProcessing
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
abstract class BatchApplicationTestSupport {

    @Autowired
    protected lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @Autowired
    protected lateinit var jobRepositoryTestUtils: JobRepositoryTestUtils


    protected fun launchJob(job: Job) {
        jobLauncherTestUtils.job = job
    }



}

//@Configuration
//@EnableAutoConfiguration
//@EnableBatchProcessing
//class TestBatchConfig