package com.batch.task

import com.batch.payment.domain.payment.Payment
import com.batch.payment.domain.payment.QPayment
import com.batch.task.support.test.BatchJobTestSupport
import org.junit.jupiter.api.Test
import org.springframework.batch.core.Job

internal class CsvWriterJobConfigurationTest(
    private val csvWriterJob: Job,
) : BatchJobTestSupport() {

    @Test
    internal fun `csvWriterJob test`() {
        //given
        (1..10).map {
            Payment(
                amount = it.toBigDecimal(),
                orderId = it.toLong()
            )
        }
            .persist()

        //when
//        launchJob(csvWriterJob)
        launchStep(csvWriterJob, "csvWriterStep")

        //then
        thenJobCompleted()

        deleteAll(QPayment.payment)
    }
}