package com.example.querydsl.domain

import javax.persistence.*


@Entity
@Table(name = "member")
data class Member(
        @Column(name = "username", nullable = false)
        private var username: String,

        @Column(name = "age", nullable = false)
        private var age: Int = 0,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "team_id")
        private var team: Team? = null
) : EntityAuditing() {

    fun changeTeam(team: Team) {
        this.team = team
        team.members.add(this)

    }


}