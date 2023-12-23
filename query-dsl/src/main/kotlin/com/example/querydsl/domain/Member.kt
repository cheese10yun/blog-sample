package com.example.querydsl.domain

import org.hibernate.annotations.Where
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table


@Entity
@Table(name = "member")
@Where(clause = "status = 'NORMAL'")
data class Member(
    @Column(name = "username", nullable = false)
    var username: String,

    @Column(name = "age", nullable = false)
    var age: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: MemberStatus = MemberStatus.NORMAL,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id", nullable = false)
    var team: Team
) : EntityAuditing() {
}

enum class MemberStatus {
    NORMAL,
    BAN
}