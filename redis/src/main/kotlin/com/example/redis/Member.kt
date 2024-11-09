package com.example.redis

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.repository.CrudRepository

@RedisHash(value = "member")
data class Member(
    @Id
    var id: String? = null,

    @TimeToLive
    val ttl: Long = 20
) {
    var name: String = "name-$id"
}

interface MemberRepository : CrudRepository<Member, Long>