package com.example.querydsl.service

import com.example.querydsl.domain.Team
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ApiService(
    private val restTemplate: RestTemplate
) {

    fun getTeam(name: String): List<Team> {
        return restTemplate.getForObject("/teams?name=$name", Array<Team>::class.java)!!.toList()
    }
}