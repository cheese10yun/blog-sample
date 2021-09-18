package com.batch.task

import com.batch.payment.domain.payment.PaymentRepository
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlGroup

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
//@Transactional
class SqlTest(
    private val paymentRepository: PaymentRepository
) {

    @AfterAll
    internal fun setUp() {
        println("======AfterAll======")
        println("payments size ${paymentRepository.findAll().size}")
        println("======AfterAll======")
    }

//    @Sql("/payment-setup.sql")
//    @Test
//    fun `sql test code`() {
//        //given
//
//        //when
//        val payments = paymentRepository.findAll().toList()
//
//        //then
//        then(payments).hasSize(12)
//        println("sql test code")
//    }

    @SqlGroup(
        Sql(
            value = ["/schema.sql", "/payment-setup.sql"],
            config = SqlConfig(
                dataSource = "dataSource",
                transactionManager = "transactionManager"
            ),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
        ),
        Sql(
            value = ["/delete.sql"],
            config = SqlConfig(
                dataSource = "dataSource",
                transactionManager = "transactionManager"
            ),
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
        )
    )
    @Test
    fun `sql test code2`() {
        //given
        //when
        val payments = paymentRepository.findAll().toList()
        //then
        then(payments).hasSize(12)
        println("sql test code")
    }
}