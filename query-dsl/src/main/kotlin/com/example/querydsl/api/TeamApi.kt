package com.example.querydsl.api

import com.example.querydsl.domain.Team
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import com.example.querydsl.domain.QTeam.team as qTeam

@RestController
@RequestMapping("/teams")
class TeamApi(
    private val query: JPAQueryFactory
) {

    @GetMapping
    fun get(@RequestParam(required = false) name: String?): List<Team> {
        return query.selectFrom(qTeam)
            .from(qTeam)
            .where(
                qTeam.name.like("%$name%")
            )
            .fetch()
    }
}