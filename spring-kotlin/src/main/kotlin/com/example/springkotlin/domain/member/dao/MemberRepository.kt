package com.example.springkotlin.domain.member.dao

import com.example.springkotlin.domain.member.domain.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long>{
}