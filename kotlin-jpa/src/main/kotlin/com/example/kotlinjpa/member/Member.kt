package com.example.kotlinjpa.member

import javax.persistence.*


@Table(name = "member")
@Entity
data class Member(
        @Id
        @GeneratedValue
        var id: Long = 0,

        @Column(name = "email", nullable = false, updatable = false)
        var email: String,

        @Column(name = "name", nullable = false)
        var name: String
)
