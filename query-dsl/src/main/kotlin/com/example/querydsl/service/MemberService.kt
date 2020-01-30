package com.example.querydsl.service

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class MemberService(
        private val restTemplate: RestTemplate
) {

    fun get(): String {
        val forEntity = restTemplate.getForEntity("https://www.naver.com/", String::class.java)
        return forEntity.body!!
    }
}