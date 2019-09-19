package com.example.springkotlin.member.dao

import com.example.springkotlin.member.domain.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long>{
}