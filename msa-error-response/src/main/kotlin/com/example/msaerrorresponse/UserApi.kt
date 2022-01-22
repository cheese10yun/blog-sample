package com.example.msaerrorresponse

import javax.validation.Valid
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