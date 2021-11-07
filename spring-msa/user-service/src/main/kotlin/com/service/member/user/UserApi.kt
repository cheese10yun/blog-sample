package com.service.member.user

import com.service.member.client.OrderResponse
import io.github.resilience4j.bulkhead.annotation.Bulkhead
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.micrometer.core.annotation.Timed
import java.time.LocalDateTime
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import kotlin.random.Random
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserApi(
    private val userSignUpService: UserSignUpService,
    private val userFindService: UserFindService
) {


    @PostMapping
    fun signUp(@RequestBody @Valid dto: UserSignUpRequest) =
//        UserResponse(userSignUpService.signUp(dto))
        userSignUpService.signUp(dto)

    @GetMapping
    fun getUsers(
        @PageableDefault(sort = ["id"], direction = Sort.Direction.DESC) pageAble: Pageable
    ) =
        userFindService.findAll(pageAble)

    @GetMapping("/{id}")
    fun getUser(
        @PathVariable id: Long
    ) = userFindService.findById(id)


    @GetMapping("/{userId}/orders")
//    @Timed(value = "users.bbb", longTask = true)
    fun getUserWithOrderBy(
        @PathVariable userId: String
    ) = userFindService.findWithOrder(userId)

    @GetMapping("/{userId}/orders/test")
    fun getUserWithOrderByTest(
        @PathVariable userId: String,
        @RequestParam(value = "delay", defaultValue = "0") delay: Int = 0,
        @RequestParam(value = "faultPercentage", defaultValue = "0") faultPercentage: Int = 0
    ): UserWithOrderResponse {
        Thread.sleep(delay.toLong())
        val random = Random.nextInt(0, 10)
        if (faultPercentage > random) {
            throw IllegalArgumentException("faultPercentage Error...")
        }
        return userFindService.findWithOrder(userId)
    }

}

data class UserSignUpRequest(
    @field:Email
    val email: String,
    @field:NotEmpty
    val name: String,
    @field:NotEmpty
    val password: String
) {
    fun toEntity(password: String) =
        User(
            email = this.email,
            name = this.name,
            password = password,
            userId = UUID.randomUUID().toString()
        )
}

class UserResponse(user: User) {
    val email = user.email
    val name = user.name
    val userid = user.userId
    val password = user.password
}

class UserWithOrderResponse(
    user: User,
    val orders: List<OrderResponse>
) {
    val email = user.email
    val name = user.name
    val userid = user.userId
    val password = user.password
}

class OrderResponse(
    val productId: String,
    val qty: Int,
    val unitPrice: Int,
    val totalPrice: Int,
    val createdAt: LocalDateTime
)