package com.example.batch.service


import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
@ActiveProfiles("test")
internal class PaymentRestServiceTest(
    private val paymentRestService: PaymentRestService
)  {


    @Test
    internal fun asd() {
        val page = paymentRestService.requestPayment(BigDecimal.valueOf(20), 0, 10)

        val contents = page.content

        for (content in contents) {
            println(content)
        }
    }
}