package com.example.springkotlin.member

import javax.persistence.*


@Entity
@Table(name = "member")
@Access(AccessType.FIELD) // 용도는 ?
class Member protected constructor() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "email", nullable = false, unique = true)
    lateinit var email: String
        protected set

    @Column(name = "name", nullable = false, unique = true)
    lateinit var name: String
        protected set

    constructor(email: String, name: String) : this() {
        this.email = email
        this.name = name
    }


}