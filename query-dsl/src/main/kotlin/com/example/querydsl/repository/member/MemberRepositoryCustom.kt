package com.example.querydsl.repository.member

import com.example.querydsl.domain.Member
import com.example.querydsl.dto.MemberDtoQueryProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface MemberRepositoryCustom {

    fun search(username: String?, age: Int?): Member
    fun search(username: String?, age: Int?, page: Pageable): Page<MemberDtoQueryProjection>

}