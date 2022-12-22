package com.example.stock.domain

import org.redisson.api.RedissonClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit

@Service
class RedissonLockStockFacadeService(
    private val redissonClient: RedissonClient,
    private val stockService: StockService,
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun decrease(stockId: Long, quantity: Long) {
        val lock = redissonClient.getLock("stockId:$stockId")
        try {
            val available = lock.tryLock(5, 1, TimeUnit.SECONDS)
            if (available.not()) {
                println("lock obtain failed")
                return
            }
            stockService.decrease(
                stockId = stockId,
                quantity = quantity
            )
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } finally {
            lock.unlock()
        }
    }
}
