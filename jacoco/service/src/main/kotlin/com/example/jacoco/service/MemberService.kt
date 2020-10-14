package com.example.jacoco.service

import com.example.jacoco.domain.Member

class MemberService {

    fun findByName(name: String) = Member(name = name, email = "asd@asd.com")
}