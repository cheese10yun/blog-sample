package com.service.member.user

import com.service.member.client.OrderResponse
import com.service.member.logger
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import org.springframework.cloud.sleuth.Tracer
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
        private val userFindService: UserFindService,
        private val tracer: Tracer
) {

    private val log by logger()

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


//    @GetMapping("/{userId}/orders")
////    @Timed(value = "users.bbb", longTask = true)
//    fun getUserWithOrderBy(
//        @PathVariable userId: String
//    ) = userFindService.findWithOrder(userId)

    @GetMapping("/{userId}/orders")
    fun getUserWithOrderByTest(
        @PathVariable userId: String,
        @RequestParam(value = "delay", defaultValue = "0") delay: Int = 0,
        @RequestParam(value = "faultPercentage", defaultValue = "0") faultPercentage: Int = 0
    ): UserWithOrderResponse {

        val currentSpan = tracer.currentSpan()
        val nextSpan = tracer.nextSpan()
        val span = currentSpan ?: nextSpan
        val context = span.context()

        log.info("=======register======")
        log.error("current traceId: ${currentSpan?.context()?.traceId()}")
        log.error("current spanId: ${currentSpan?.context()?.spanId()}")
        log.error("current parentId: ${currentSpan?.context()?.parentId()}")
        log.error("current sampled: ${currentSpan?.context()?.sampled()}")

        log.error("next traceId: ${nextSpan.context().traceId()}")
        log.error("next spanId: ${nextSpan.context().spanId()}")
        log.error("next parentId: ${nextSpan.context().parentId()}")
        log.error("next sampled: ${nextSpan.context().sampled()}")
        log.info("=======register======")
        return userFindService.findWithOrder(userId, faultPercentage, delay)
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

//data class OrderResponse(
//    val productId: String,
//    val qty: Int,
//    val unitPrice: Int,
//    val totalPrice: Int,
//    val createdAt: LocalDateTime
//)