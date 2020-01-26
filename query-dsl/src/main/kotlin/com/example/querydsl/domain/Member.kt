package com.example.querydsl.domain

import javax.persistence.*


@Entity
@Table(name = "member")
data class Member(
        @Column(name = "username", nullable = false)
        var username: String,

        @Column(name = "age", nullable = false)
        var age: Int = 0,

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "team_id", nullable = false)
        var team: Team
) : EntityAuditing() {

    fun changeTeam(team: Team) {
        this.team = team
        team.members.add(this)
    }


}