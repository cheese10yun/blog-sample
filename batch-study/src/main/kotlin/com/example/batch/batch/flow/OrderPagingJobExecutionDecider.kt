package com.example.batch.batch.flow

import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.job.flow.FlowExecutionStatus
import org.springframework.batch.core.job.flow.JobExecutionDecider
import java.util.*

class OrderPagingJobExecutionDecider : JobExecutionDecider {

    override fun decide(jobExecution: JobExecution, stepExecution: StepExecution?): FlowExecutionStatus {
        if (Random().nextInt() > 0) {
            println("FlowExecutionStatus.COMPLETED")
            return FlowExecutionStatus.COMPLETED
        }

        println("FlowExecutionStatus.FAILED")
        return FlowExecutionStatus.FAILED
    }
}