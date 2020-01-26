package com.example.querydsl.dto

import com.querydsl.core.annotations.QueryProjection


data class MemberDto @QueryProjection constructor(
        val username: String,
        val age: Int) {
}
