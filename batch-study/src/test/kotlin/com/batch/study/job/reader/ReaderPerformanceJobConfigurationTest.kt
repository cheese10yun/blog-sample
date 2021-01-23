package com.batch.study.job.reader

import com.batch.study.BatchApplicationTestSupport
import org.junit.jupiter.api.Test
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.JobRepositoryTestUtils
import org.springframework.beans.factory.annotation.Autowired


class ReaderPerformanceJobConfigurationTest(
    jobBuilderFactory: JobBuilderFactory,
    readerPerformanceStep: Step,
     private val readerPerformanceJob: Job
) : BatchApplicationTestSupport() {

//    private val job = jobBuilderFactory["readerPerformanceJob"]
//        .incrementer(RunIdIncrementer())
//        .start(readerPerformanceStep)
//        .build()


    @Test
    internal fun asd() {

        val jobParameters = JobParametersBuilder()
            .addString("orderDate", "2020-12-12")
            .toJobParameters()


//        jobLauncherTestUtils.launchJob(jobParameters)

        launchJob(readerPerformanceJob)


    }
}