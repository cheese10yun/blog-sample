package com.service.member.user

import java.time.LocalDateTime
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import org.springframework.core.env.Environment
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserApi(
    val environment: Environment,
    val userSignUpService: UserSignUpService,
    val userFindService: UserFindService
) {

    @GetMapping("/welcome")
    fun welcome(): String? {
        return environment.getProperty("getting.message")
    }

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

class OrderResponse(
    val productId: String,
    val qry: Int,
    val unitPrice: Int,
    val totalPrice: Int,
    val createdAt: LocalDateTime
)