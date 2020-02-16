//package com.example.batch.batch.core
//
//import com.example.batch.TestBatchConfig
//import com.example.batch.domain.order.dao.PaymentRepository
//import com.example.batch.domain.order.domain.Payment
//import org.assertj.core.api.BDDAssertions.then
//import org.junit.jupiter.api.Test
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.test.context.ActiveProfiles
//import org.springframework.test.context.TestConstructor
//import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
//import java.math.BigDecimal
//import javax.persistence.EntityManagerFactory
//import com.example.batch.domain.order.domain.QPayment.payment as qPayment
//
//@SpringBootTest(classes = [TestBatchConfig::class])
//@SpringJUnitConfig
//@ActiveProfiles("test")
//@EnableAutoConfiguration
//@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
//class QuerydslPagingItemReaderTest(
//    private val paymentRepository: PaymentRepository,
//    private val entityManagerFactory: EntityManagerFactory
//) {
//
//    @Test
//    internal fun `reader test`() {
//        //given
//        val targetAmount = BigDecimal.TEN
//        paymentRepository.saveAll(listOf(
//            Payment(targetAmount),
//            Payment(targetAmount)
//        ))
//
//
//        val pageSize = 1
//        val reader = QuerydslPagingItemReader<Payment>(
//            "reader",
//            pageSize,
//            entityManagerFactory
//        ) {
//            it
//                .selectFrom(qPayment)
//                .where(qPayment.amount.gt(targetAmount))
//        }
//        //when
//
//        val read1 = reader.read()!!
//        val read2 = reader.read()!!
//        val read3 = reader.read()
//
//        //then
//
//        then(read1.amount).isEqualTo(targetAmount)
//        then(read2.amount).isEqualTo(targetAmount)
//        then(read3).isNull()
//    }
//}