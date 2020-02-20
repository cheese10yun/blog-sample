package com.example.querydsl.dto

import com.querydsl.core.annotations.QueryProjection


data class MemberDtoQueryProjection @QueryProjection constructor(
    val username: String,
    val age: Int
)

class MemberDtoBean {
    var username: String? = null
    var age: Int? = null
}

data class MemberDtoConstructor(
    val username: String,
    val age: Int
)