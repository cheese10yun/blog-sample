package com.example.msaerrorresponse

import javax.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/a-service")
class AServiceApi(
    private val userRegistrationService: UserRegistrationService
) {

    @PostMapping
    fun aService(@RequestBody @Valid dto: UserRegistrationRequest) =
        userRegistrationService.register(dto)
}