package com.example.batch.service


import com.example.batch.SpringBootTestSupport
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
internal class RestServiceTest(
    private val restService: RestService
)  {


    @Test
    internal fun asd() {
        restService.requestPayment(BigDecimal.valueOf(20), 10, 1)
    }
}