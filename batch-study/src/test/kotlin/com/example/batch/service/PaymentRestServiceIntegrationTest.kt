package com.example.batch.service


import com.example.batch.SpringBootTestSupport
import com.example.batch.domain.order.domain.Payment
import org.junit.jupiter.api.Test
import java.math.BigDecimal


internal class PaymentRestServiceIntegrationTest(
    private val paymentRestService: PaymentRestService
) : SpringBootTestSupport() {


    @Test
    internal fun `requestPayment test`() {

        val page = paymentRestService.requestPage<Payment>(BigDecimal(10), 0, 10)


        val content = page.content

        for (payment in content) {

            println(payment)
        }

    }

    @Test
    internal fun `requestPayment2 test`() {

        val payments = paymentRestService.requestPayment2()
        for (payment in payments) {
            println(payment)
        }
    }
}