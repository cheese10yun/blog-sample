package com.example.jpanplus1.member

interface MemberRepositorySupport  {
    fun findMemberAll(): MutableList<MemberDto>
}