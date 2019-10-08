package com.example.springkotlin.domain.member.domain

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime
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

    @CreatedDate
    @Column(name ="created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @LastModifiedDate
    @Column(name ="updated_ay_at", nullable = false)
    lateinit var updatedAt: LocalDateTime

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