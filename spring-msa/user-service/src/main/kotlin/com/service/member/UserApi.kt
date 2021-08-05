package com.service.member

import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserApi(
    val environment: Environment
) {

    @GetMapping("/welcome")
    fun welcome(): String? {
        return environment.getProperty("getting.message")
    }
}


// 3