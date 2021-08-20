package com.example.redis

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import org.springframework.data.repository.CrudRepository

@RedisHash("SyncRedis")
class SyncRedis(
    @Indexed
    var id: Long,
    @Indexed
    var payoutGroupId: Long,
    @Indexed
    var groupId: String,
    var partnerId: Long?
) {
    @Id
    var key = "id:$id:channelType:$groupId"
}


interface SyncRedisRepository : CrudRepository<SyncRedis, Long> {
    fun findByPayoutGroupIdAndGroupId(
        payoutGroupId: Long,
        groupId: String
    ): List<SyncRedis>
}