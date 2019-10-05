package com.example.springkotlin.domain.member.dto

import com.example.springkotlin.domain.member.domain.Member
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

class MemberSignUpRequest {


    @Email
    lateinit var email: String

    @NotEmpty
    lateinit var name: String

    fun toEntity() : Member{
        return Member(email, name)
    }
}