package com.example.intellijtest

import org.springframework.stereotype.Service

@Service
class MemberRegistrationService(
    private val memberRegistrationValidatorService: MemberRegistrationValidatorService
) {

    fun register(dto: MemberRegistrationRequest) {
        memberRegistrationValidatorService.checkEmailDuplication(dto.email)
    }
}