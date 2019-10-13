package com.example.springkotlin.domain.transaction

import org.junit.Test

class TransactionTest {

    @Test
    fun name() {
        val transaction = Transaction.newInstance(
                code = "code",
                paymentMethodType = PaymentMethodType.CARD,
                thirdPartyTransactionId = "123"
        )
        println(transaction.code)
        println(transaction.paymentMethodType)
        println(transaction.partnerTransactionId)
    }

}

