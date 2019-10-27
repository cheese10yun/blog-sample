package com.example.jpanplus1.member

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MemberRepository : JpaRepository<Member, Long>, MemberRepositorySupport {

    @Query(
            "select m from Member m join fetch m.orders"
    )
    fun findAllWithFetch(): List<Member>


    @Query(
            value = "select m from Member m left join fetch m.orders",
            countQuery = "select count(m) from Member m"
    )
    fun findAllWithFetchPaging(pageable: Pageable): Page<Member>


    @Query(
            value = "select  m from Member m left join fetch m.orders left join fetch m.coupons",
            countQuery = "select count(m) from Member m"
    )
    fun findAllWithFetchPaging2(pageable: Pageable): Page<Member>
}