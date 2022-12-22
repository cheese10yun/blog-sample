package com.example.stock.domain

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class RedisLockRepository(
    private val redisTemplate: RedisTemplate<String, String>,
) {


    fun lock(key: Long) =
        redisTemplate
            .opsForValue()
            .setIfAbsent(
                key.toString(),
                "lock",
                Duration.ofMillis(3_000)
            )!!

    fun unLock(key: Long) =
        redisTemplate
            .delete(key.toString())
}

@Service
class LettuceLockStockFacadeService(
    private val redisLockRepository: RedisLockRepository,
    private val stockService: StockService,
) {


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun decrease(stockId: Long, quantity: Long) {

        while (redisLockRepository.lock(key = stockId).not()) {
            Thread.sleep(100)
        }

        try {
            stockService.decrease(
                stockId = stockId,
                quantity = quantity
            )
        } finally {
            redisLockRepository.unLock(stockId)
        }
    }
}