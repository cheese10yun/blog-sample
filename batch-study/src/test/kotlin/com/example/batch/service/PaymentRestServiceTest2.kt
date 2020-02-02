package com.example.batch.service


import com.example.batch.SpringBootTestSupport
import org.junit.jupiter.api.Test


internal class PaymentRestServiceTest2(
    private val paymentRestService: PaymentRestService
) : SpringBootTestSupport() {



    @Test
    internal fun `222 mock test`() {

        val payments = paymentRestService.requestPayment2()

        for (payment in payments) {

            println(payment)
        }

    }
}