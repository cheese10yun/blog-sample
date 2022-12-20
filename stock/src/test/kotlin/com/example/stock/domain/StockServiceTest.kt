package com.example.stock.domain

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
class StockServiceTest(
    private val stockService: StockService,
    private val stockRepository: StockRepository
) {

    @Test
    fun name() {
        //given
        val stockId = stockRepository.save(
            Stock(
                productId = 1L,
                quantity = 100L
            )
        ).id!!

        //when

        stockService.decrease(
            stockId = stockId,
            quantity = 1L
        )

        //then
        val stock = stockRepository.getReferenceById(stockId)

        then(stock.quantity).isEqualTo(99)

    }
}