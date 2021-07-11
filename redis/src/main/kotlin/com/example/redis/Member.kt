package com.example.redis

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.repository.CrudRepository

@RedisHash("member")
data class Member(
    @Id
    var id: Long? = null
) {

    var name: String = "name-$id"
}

interface MemberRepository : CrudRepository<Member, Long>