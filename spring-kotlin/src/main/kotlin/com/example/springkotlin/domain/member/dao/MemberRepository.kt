package com.example.springkotlin.domain.member.dao

import com.example.springkotlin.domain.member.domain.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MemberRepository : JpaRepository<Member, Long> {

    @Modifying(clearAutomatically = true)
    @Query(
        "update Member m set m.name = 'none_name' " +
                "where m.id in :ids "
    )
    fun updateName(@Param("ids") ids: List<Long>): Int

}