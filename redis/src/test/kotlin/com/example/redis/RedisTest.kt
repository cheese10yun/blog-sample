package com.example.redis

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.TestConstructor


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RedisTest(
    val redisTemplate: RedisTemplate<String, String>,
) {

    @Test
    fun testStrings() {
        val key = "redis-key"

        redisTemplate.opsForValue()[key] = "1"
        println("result_1 = ${redisTemplate.opsForValue()[key]}")

        redisTemplate.opsForValue().increment(key) // redis incr 명령어
        println("result_2 = ${redisTemplate.opsForValue()[key]}")
    }

    @Test
    fun list() {
        val key = "redis-key-list-2"
        val list = redisTemplate.opsForList()

        list.rightPush(key, "11")
        list.rightPush(key, "22")
        list.rightPush(key, "33")
        list.rightPush(key, "55")

        list.rightPushAll(key, "aa", "bb", "cc", "dd")


        println("index 0: ${list.index(key, 0)}")
        println("size: ${list.size(key)}")

        val range = list.range(key, 0, 9)
        println("range: $range")
    }

    @Test
    fun testSet() {
        val set = redisTemplate.opsForSet()
        set.add("11", "a11", "b11", "c11")
        set.add("22", "a22", "b22", "c33")
        set.add("33", "a33", "b33", "c33")

        println("key 11: ${set.size("11")}")
        println("key 22: ${set.size("22")}")

        val difference = set.difference("11", "22")
        println("difference: $difference")
    }
}