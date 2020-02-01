package com.example.querydsl

import com.example.querydsl.domain.Team
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@Component
@Profile("default")
@Transactional
class AppRunner(
    private val em: EntityManager
) : ApplicationRunner {


    override fun run(args: ApplicationArguments?) {

        val teams = listOf(
            Team("team 1"),
            Team("team 2"),
            Team("team 3"),
            Team("team 4"),
            Team("team 5"),
            Team("team 6"),
            Team("team 7"),
            Team("team 8"),
            Team("team 9")
        )

        for (team in teams) {
            em.persist(team)
        }

    }
}