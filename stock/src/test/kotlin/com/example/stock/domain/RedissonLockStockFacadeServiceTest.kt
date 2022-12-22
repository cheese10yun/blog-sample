package com.example.stock.domain

import org.assertj.core.api.BDDAssertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.TestConstructor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RedissonLockStockFacadeServiceTest(
    private val redissonLockStockFacadeService: RedissonLockStockFacadeService,
    private val stockRepository: StockRepository,
) {
    @Test
    fun `동시에 100개 요청`() {
        //given
        val quantity = 100L
        val stockId = stockRepository.save(
            Stock(
                productId = 1L,
                quantity = quantity
            )
        ).id!!
        val threadCount = 20
        val latch = CountDownLatch(threadCount)

        //when
        val executorService = Executors.newFixedThreadPool(32)
        (1..threadCount).forEach { _ ->
            executorService.submit {
                try {
                    redissonLockStockFacadeService.decrease(stockId, 1L)
                } finally {
                    latch.countDown()
                }
            }
        }
        latch.await()

        //then
        val stock = stockRepository.findByIdOrNull(stockId)!!
        BDDAssertions.then(stock.quantity).isEqualTo(quantity - threadCount)
    }
}