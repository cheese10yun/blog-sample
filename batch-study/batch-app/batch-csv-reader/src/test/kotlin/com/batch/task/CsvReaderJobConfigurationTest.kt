package com.batch.task

import com.batch.payment.domain.payment.PaymentRepository
import com.batch.payment.domain.payment.QPayment
import com.batch.task.support.test.BatchTestSupport
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.batch.core.Job

internal class CsvReaderJobConfigurationTest(
    private val csvReaderJob: Job,
    private val paymentRepository: PaymentRepository
) : BatchTestSupport() {

    @Test
    internal fun `csvReaderJob JPAQueryFactory 기반 테스트`() {
        //given

        //when
        launchJob(csvReaderJob)

        //then
        thenBatchCompleted()

        val payments = query.selectFrom(QPayment.payment)
            .where(QPayment.payment.orderId.eq(1L))
            .fetch()

        then(payments).hasSize(9)

        deleteAll(QPayment.payment)
    }

    @Test
    internal fun `csvReaderJob repository 기반 테스트`() {
        //given

        //when
        launchJob(csvReaderJob)

        //then
        thenBatchCompleted()

        val payments = paymentRepository.findByOrderId(1L)

        then(payments).hasSize(9)

        deleteAll(QPayment.payment)
    }
}