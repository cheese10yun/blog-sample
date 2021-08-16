package com.service.member.user

import com.service.member.client.OrderClient
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
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

    @CircuitBreaker(
        name = "findWithOrder",
        fallbackMethod = "findWithOrderFallback"
    )
    fun findWithOrder(userId: String): UserWithOrderResponse {
        val user = findByUserId(userId)
        if (true) {
            throw RuntimeException("faultPercentage Error...")
        }

        return UserWithOrderResponse(
            user = user,
            orders = orderClient.getOrderByUserId(userId)
        )
    }

    private fun findWithOrderFallback(t: Throwable): UserWithOrderResponse {
        return UserWithOrderResponse(
            user = User(
                email = "asd@asd.cm",
                name = "yu222n",
                userId = "5566da6f-3f03-4ce5-8863-3c142e452522",
                password = "\$2a\$10\$Inf2wE5nDnN/4pynduvud.h7sVm5TuNcvPt5m9r8ZpCoJCiAWjWzu"
            ),
            orders = emptyList()
        )
    }
}