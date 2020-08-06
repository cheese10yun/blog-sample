//package com.example.querydsl.service
//
//import com.example.querydsl.api.PaymentApi
//import com.example.querydsl.domain.Payment
//import com.example.querydsl.domain.QPayment
//import com.example.querydsl.repository.payment.PaymentRepository
//import com.querydsl.jpa.impl.JPAQueryFactory
//import org.springframework.stereotype.Service
//import org.springframework.transaction.annotation.Transactional
//import java.math.BigDecimal
//
//@Service
//@Transactional(readOnly = true)
//class PaymentService(
//    private val bankClient: BankClient,
//    private val query: JPAQueryFactory,
//    private val paymentRepository: PaymentRepository
//) {
//
//    @Transactional
//    fun paymentZero(targetAmount: BigDecimal) {
//
//        val payments = query.selectFrom(QPayment.payment)
//            .where(QPayment.payment.amount.gt(targetAmount))
//            .fetch()
//
//        for (payment in payments) {
//            payment.amount = BigDecimal.ZERO
//        }
//    }
//
//    @Transactional
//    fun save(amount: BigDecimal): Payment {
//        return paymentRepository.save(Payment(amount))
//    }
//
//    fun doPayment(dto: PaymentApi.BankAccountPayment) {
//        val isMatched = bankClient.requestAccount(dto.bankAccount, dto.bankAccount, dto.bankCode)
//        if (isMatched.not()) {
//            throw RuntimeException("계좌번호 계좌주명 불일치")
//        }
//        // 무통장 결제 진행...
//    }
//}