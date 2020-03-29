package com.example.springmocktest.api

import com.example.springmocktest.domain.partner.PartnerRegistrationService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

@RestController
@RequestMapping("/partners")
class PartnerApi(
    private val partnerRegistrationService: PartnerRegistrationService
) {

    @PostMapping
    fun register(@RequestBody @Valid dto: PartnerRegistrationRequest) {
        partnerRegistrationService.register(dto)
    }
}

data class PartnerRegistrationRequest(
    @field:NotEmpty
    val name: String,
    @field:NotEmpty
    val accountHolder: String,
    @field:NotEmpty
    val accountNumber: String
)