package com.example.redis

import kotlinx.coroutines.delay
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userCacheService:UserCacheService
) {

    fun getUserProfile(userId: String): UserProfile {
        val userName = userCacheService.getUserName(userId)
        val userAge = userCacheService.getUserAge(userId)
        return UserProfile(
            name = userName,
            age = userAge
        )
    }
}

@Service
class UserCacheService{

    @Cacheable(cacheNames = ["userAgeCache"], key = "#userId")
    fun getUserAge(userId: String): Int {
        Thread.sleep(500)
        return when(userId){
            "A" -> 28
            "B" -> 32
            else -> 0
        }
    }

    @Cacheable(cacheNames = ["userNameCache"], key = "#userId")
    fun getUserName(userId: String): String {
        Thread.sleep(500)
        return when(userId){
            "A" -> "Yun"
            "B" -> "Joo"
            else -> "Koo"
        }
    }
}