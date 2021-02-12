package com.batch.task

import com.batch.payment.domain.payment.Payment
import com.batch.payment.domain.payment.PaymentRepository
import com.batch.payment.domain.payment.QPayment
import com.batch.task.support.listener.SimpleJobListener
import com.batch.task.support.test.BatchTestSupport
import org.junit.jupiter.api.Test
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.test.context.jdbc.Sql

internal class CsvWriterConfigurationTest(
    private val csvWriterJob: Job,
    private val paymentRepository: PaymentRepository,
    csvWriterStep: Step,
    jobBuilderFactory: JobBuilderFactory,
) : BatchTestSupport() {

    private val job = jobBuilderFactory["csvWriterStepForTestJob"]
        .incrementer(RunIdIncrementer())
        .listener(SimpleJobListener())
        .start(csvWriterStep)
        .build()

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
        launchJob(csvWriterJob)

        //then
        thenBatchCompleted()

        deleteAll(QPayment.payment)
    }

    @Test
    internal fun `csvWriterStep test`() {
        //given
        (1..10).map {
            Payment(
                amount = it.toBigDecimal(),
                orderId = it.toLong()
            )
        }
            .persist()

        //when
        launchStep("csvWriterStep")

        //then
        thenBatchCompleted()

        deleteAll(QPayment.payment)
    }

    @Test
    internal fun `csvWriterStep job을 직접 생성해서 테스트`() {
        //given
        (1..10).map {
            Payment(
                amount = it.toBigDecimal(),
                orderId = it.toLong()
            )
        }
            .persist()

        //when
        launchJob(job)

        //then
        thenBatchCompleted()

        deleteAll(QPayment.payment)
    }

    @Test
    internal fun `csvWriterJob repositroy를 DI 받아 테스트 진행`() {
        //given
        (1..10).map {
            Payment(
                amount = it.toBigDecimal(),
                orderId = it.toLong()
            )
        }
            .also {
                paymentRepository.saveAll(it)
            }

        //when
        launchJob(csvWriterJob)

        //then
        thenBatchCompleted()

        deleteAll(QPayment.payment)
    }

    @Test
    @Sql("/csv-setup.sql")
    internal fun `csvWriterJob sql 테스트 진행`() {
        //given

        //when
        launchJob(csvWriterJob)

        //then
        thenBatchCompleted()

        deleteAll(QPayment.payment)
    }


    @Test
    internal fun `csvWriterJob 테스트 코드만을 위한 코드`() {
        //given

        //when
        launchJob(csvWriterJob)

        //then
        thenBatchCompleted()
        // paymentRepository.findXXXXX()

        deleteAll(QPayment.payment)
    }
}