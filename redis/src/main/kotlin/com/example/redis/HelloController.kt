package com.example.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpSession

@RestController
class HelloController(
    private val redisTemplate: StringRedisTemplate
) {

    @GetMapping("/hello")
    fun hello(): String {
        return "Hello"
    }

    @GetMapping("/setFruit")
    fun setFruit(
        @RequestParam name: String
    ): String {
        val ops = redisTemplate.opsForValue()
        ops.set("fruit", name)
        return "saved"
    }

    @GetMapping("/getFruit")
    fun getFruit(): String? {
        val ops = redisTemplate.opsForValue()
        return ops.get("fruit")
    }

    @GetMapping("/login")
    fun login(
        session: HttpSession,
        @RequestParam name: String
    ): String {

        session.setAttribute("name", name)
        return "saved"
    }

    @GetMapping("/myName")
    fun myName(session: HttpSession): String {

        return session.getAttribute("name").toString()
    }
}