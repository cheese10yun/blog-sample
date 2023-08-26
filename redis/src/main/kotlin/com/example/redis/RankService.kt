package com.example.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class RankService(
    private val redisTemplate: StringRedisTemplate
) {
    val LEADERBOARD_KEY = "leaderBoard"

    fun setUserScore(userId: String, score: Int): Boolean {
        val zSetOps = redisTemplate.opsForZSet()
        zSetOps.add(LEADERBOARD_KEY, userId, score.toDouble())
        return true
    }

    fun getUserRanking(userId: String): Long? {
        val zSetOps = redisTemplate.opsForZSet()
        val rank = zSetOps.reverseRank(LEADERBOARD_KEY, userId)
        return rank
    }


    fun getTopRan(limit: Long): List<String> {
        val zSetOps = redisTemplate.opsForZSet()
        val rank = zSetOps.reverseRange(LEADERBOARD_KEY, 0, limit - 1)
        return when (rank) {
            null -> emptyList()
            else -> rank.toList()
        }
    }
}