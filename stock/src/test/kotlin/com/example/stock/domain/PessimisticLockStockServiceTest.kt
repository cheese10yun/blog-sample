package com.example.stock.domain

import org.assertj.core.api.BDDAssertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.TestConstructor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors


@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PessimisticLockStockServiceTest(
    private val pessimisticLockStockService: PessimisticLockStockService,
    private val stockRepository: StockRepository,
) {

    @Test
    fun `동시에 100개 요청`() {
        //given
        val stockId = stockRepository.save(
            Stock(
                productId = 1L,
                quantity = 100L
            )
        ).id!!
        val threadCount = 100
        val latch = CountDownLatch(threadCount)

        //when
        val executorService = Executors.newFixedThreadPool(32)
        (1..threadCount).forEach { _ ->
            executorService.submit {
                try {
                    pessimisticLockStockService.decrease(stockId, 1L)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        //then

        val stock = stockRepository.findByIdOrNull(stockId)!!
        println("quantity ${stock.quantity}")

        BDDAssertions.then(stock.quantity).isEqualTo(0)

    }
}