package com.example.intellijtest

import org.springframework.stereotype.Service

@Service
class MemberRegistrationService(
    private val memberRegistrationValidatorService: MemberRegistrationValidatorService
) {

    /**
     * @param isAlreadyCompletedValidation true 경우 이미 유효성 검사를 진행 한것으로 간주하고 추가적으로 유효성 검사를 진행하지 않는다.
     */
    fun register(
        dto: MemberRegistrationRequest,
        isAlreadyCompletedValidation: Boolean = false // 이미 유효성 검사를 진행 했다면 추가적은 검증을 진행하지 않는다..
    ) {
        if (isAlreadyCompletedValidation.not()){
            memberRegistrationValidatorService.checkEmailDuplication(dto.email)
        }
    }
}