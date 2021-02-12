package com.batch.task.support.listener

import com.batch.task.support.logger
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener

class SimpleJobListener : JobExecutionListener {
    private val log by logger()

    override fun beforeJob(jobExecution: JobExecution) {
        log.info("beforeJob")
    }

    override fun afterJob(jobExecution: JobExecution) {
        log.info("afterJob")
    }
}