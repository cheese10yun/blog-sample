package com.example.springkotlin.domain.member.domain

import javax.persistence.*


@Entity
@Table(name = "member")
//@Access(AccessType.FIELD) // 용도는 ?
class Member protected constructor() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "email", nullable = false, unique = true)
    lateinit var email: String
        protected set

    @Column(name = "name", nullable = false)
    lateinit var name: String
        protected set

    constructor(email: String, name: String) : this() {
        this.email = email
        this.name = name
    }

    override fun toString(): String {
        return "Member(id=$id, email='$email', name='$name')"
    }

    fun updateName(name: String){
        this.name = name
    }


}