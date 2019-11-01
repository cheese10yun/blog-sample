package com.example.kotlinjunit5.member

import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository :JpaRepository<Member, Long> {
}