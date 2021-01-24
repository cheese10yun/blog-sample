package com.batch.study.job.reader

import com.batch.study.BatchApplicationTestSupport
import com.batch.study.domain.payment.Payment
import com.batch.study.domain.payment.QPayment
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource

@TestPropertySource(
    properties = [
        "args.value=string value"
    ]
)
internal class ReaderPerformanceJobConfigurationTest : BatchApplicationTestSupport() {

    @AfterEach
    internal fun tearDown() {
        query.delete(QPayment.payment)
    }

    @Test
    internal fun `test`() {
        (1..5).map {
            Payment(
                amount = it.toBigDecimal(),
                orderId = it.toLong()
            )
        }
            .toList()
            .persistAll()

        launchStep("readerPerformanceStep")
    }
}