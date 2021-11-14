package com.service.member.user

import com.service.member.client.OrderClient
import com.service.member.logger
import io.github.resilience4j.bulkhead.annotation.Bulkhead
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import kotlin.random.Random
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserFindService(
    private val userRepository: UserRepository,
    private val orderClient: OrderClient,
    private val circuitBreakerFactory: CircuitBreakerFactory<*, *>
) {
    private val log by logger()

    fun findById(id: Long) =
        userRepository.findByIdOrNull(id)

    fun findByUserId(userId: String) = userRepository.findByUserId(userId)

    fun findAll(pageAble: Pageable) =
        userRepository.findAll(pageAble)

    @Bulkhead(name = "findWithOrder")
    @CircuitBreaker(
        name = "findWithOrder",
        fallbackMethod = "findWithOrderFallback"
    )
    fun findWithOrder(
        userId: String,
        faultPercentage: Int,
        delay: Int
    ): UserWithOrderResponse {
//        Thread.sleep(delay.toLong())
//        val random = Random.nextInt(0, 100)
//        if (faultPercentage > random) {
//            throw IllegalArgumentException("faultPercentage Error...")
//        }
        val user = findByUserId(userId)
        return UserWithOrderResponse(
            user = user,
            orders = orderClient.getOrderByUserId(
                userId = userId,
                faultPercentage = faultPercentage,
                delay = delay
            )
        )
    }

    private fun findWithOrderFallback(
        userId: String,
        faultPercentage: Int,
        delay: Int,
        ex: Exception
    ): UserWithOrderResponse {
        log.error("findWithOrderFallback 발생")
        return UserWithOrderResponse(
            user = findByUserId(userId),
            orders = emptyList()
        )
    }
}