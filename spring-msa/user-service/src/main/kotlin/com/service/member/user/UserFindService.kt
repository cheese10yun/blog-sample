package com.service.member.user

import com.service.member.client.OrderClient
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
        faultPercentage: Int
    ): UserWithOrderResponse {
//        Thread.sleep(delay.toLong())
        val random = Random.nextInt(0, 100)
        if (faultPercentage > random) {
            throw IllegalArgumentException("faultPercentage Error...")
        }
        val user = findByUserId(userId)
        return UserWithOrderResponse(
            user = user,
            orders = orderClient.getOrderByUserId(userId)
        )
    }

    private fun findWithOrderFallback(t: Exception): UserWithOrderResponse {
        println("fallback 설정")
        return UserWithOrderResponse(
            user = User(
                email = "2222222222@asd.cm",
                name = "22222222",
                userId = "22222222",
                password = "\$2a\$10\$Inf2wE5nDnN/4pynduvud.h7sVm5TuNcvPt5m9r8ZpCoJCiAWjWzu"
            ),
            orders = emptyList()
        )
    }
}