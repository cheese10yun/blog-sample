package com.batch.study.listener

import com.batch.study.core.JpaSupport
import com.batch.study.domain.payment.Payment
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.stereotype.Component

@Component
class JobDataSetUpListener(
    private val jpaSupport: JpaSupport
) : JobExecutionListener {
    override fun beforeJob(jobExecution: JobExecution) {
        (1..100)
            .map { Payment(it.toBigDecimal(), it.toLong()) }
            .also { jpaSupport.saveAll(it) }
    }

    override fun afterJob(jobExecution: JobExecution): Unit = Unit
}