package com.example.batch.batch.listener

import org.slf4j.LoggerFactory
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.stereotype.Component

@Component
class InactiveJobListener : JobExecutionListener {

    private val log = LoggerFactory.getLogger(javaClass)


    override fun beforeJob(jobExecution: JobExecution) {
        log.info("beforeJob {}", jobExecution.toString())
    }

    override fun afterJob(jobExecution: JobExecution) {
        log.info("afterJob {}", jobExecution.toString())
    }
}