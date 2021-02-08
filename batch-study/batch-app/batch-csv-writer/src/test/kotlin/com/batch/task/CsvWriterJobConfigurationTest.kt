package com.batch.task

import com.batch.task.support.test.BatchJobTestSupport
import org.junit.jupiter.api.Test
import org.springframework.batch.core.Job

internal class CsvWriterJobConfigurationTest(
    private val csvWriterJob: Job
) : BatchJobTestSupport() {


    @Test
    internal fun asdasd() {
        launchJob(csvWriterJob)
    }
}