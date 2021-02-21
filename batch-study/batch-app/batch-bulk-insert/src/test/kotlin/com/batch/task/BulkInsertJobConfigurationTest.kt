package com.batch.task

import com.batch.payment.domain.payment.PaymentBackJpa
import com.batch.payment.domain.payment.PaymentBackJpaRepository
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class BulkInsertJobConfigurationTest(
    private val paymentBackJpaRepository: PaymentBackJpaRepository
) {

    @Test
    internal fun `jpa 기반 bulk insert`() {
        (1..100).map {
            PaymentBackJpa(
                amount = it.toBigDecimal(),
                orderId = it.toLong()
            )
                .apply {
//                    this.id = it.toLong() // ID를 자동 증가로 변경 했기 때문에 코드 주석
                }
        }.also {
            paymentBackJpaRepository.saveAll(it)
        }
    }
}