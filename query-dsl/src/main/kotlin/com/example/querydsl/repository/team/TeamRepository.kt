package com.example.querydsl.repository.team

import com.example.querydsl.domain.Team
import org.springframework.data.jpa.repository.JpaRepository

interface TeamRepository : JpaRepository<Team, Long> {
}