package com.example.msaerrorresponse

import javax.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.cloud.sleuth.Tracer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/b-service")
class UserApi {

    @PostMapping
    fun register(
        @RequestBody @Valid dto: UserRegistrationRequest
    ) {
        // 회원 가입 로직 수행 코드
    }

}


@RestController
@RequestMapping("/api/v1/users")
class UserApi2(
    private val userRegistrationService: UserRegistrationService,
    private val tracer: Tracer
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/{userId}")
    fun register(
        @PathVariable userId: Long
    ) = User(id = userId, name = "test")


    @GetMapping("/test")
    fun register(): User {

        val traceId = (tracer.currentSpan() ?: tracer.nextSpan()).context().traceId()
        log.error("traceId: $traceId")
        return UserClient()
                .getUser(1)
    }

}