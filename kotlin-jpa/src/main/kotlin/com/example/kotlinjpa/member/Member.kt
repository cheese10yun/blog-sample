package com.example.kotlinjpa.member

import javax.persistence.*


@Table(name = "member")
@Entity
class Member protected constructor() {

    @Id
    @GeneratedValue
    var id: Long = 0L

    @Column(name = "email", nullable = false, updatable = false)
    lateinit var email: String

    @Column(name = "name", nullable = false)
    lateinit var name: String


}
