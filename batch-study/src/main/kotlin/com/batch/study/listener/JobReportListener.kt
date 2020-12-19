package com.batch.study.listener

import com.batch.study.logger
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.StepExecution
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

class JobReportListener : JobExecutionListener {
    private val log by logger()

    override fun beforeJob(jobExecution: JobExecution): Unit = Unit

    override fun afterJob(jobExecution: JobExecution) {
        log.info(jobReport(jobExecution))
        log.info(stepReport(jobExecution.stepExecutions))
    }

    private fun jobReport(jobExecution: JobExecution): String {
        return """
            [${jobExecution.jobInstance.jobName} 리포트]
            최종상태: ${jobExecution.status}
            시작일시: ${jobExecution.createTime.isoDateTime()}
            종료일시: ${jobExecution.endTime.isoDateTime()}()
            아이디  : ${jobExecution.jobId}(인스턴스: ${jobExecution.jobInstance.id})
            매개변수: ${jobExecution.jobParameters.parameters()}
        """.trimIndent()
    }

    private fun stepReport(stepExecutions: Collection<StepExecution>): String {
        return stepExecutions.joinToString("\n") { stepExecution ->
            """                    ------
                    [${stepExecution.stepName} 리포트]
                    최종상태: ${stepExecution.exitStatus}
                    시작일시: ${stepExecution.startTime.isoDateTime()}
                    종료일시: ${stepExecution.endTime.isoDateTime()}
                    읽기갯수: ${stepExecution.readCount}
                    필터갯수: ${stepExecution.filterCount}
                    쓰기갯수: ${stepExecution.writeCount}
                    커밋갯수: ${stepExecution.commitCount}
                """.trimIndent()
        }
    }

    private fun JobParameters.parameters(separator: String = ",") =
        this.parameters
            .filterNot { it.key.startsWith("-spring") }
            .map { "${it.key}: ${it.value}" }
            .joinToString(separator)

    private fun Date.isoDateTime(): String {
        return toInstant().atZone(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ISO_DATE_TIME)
    }
}