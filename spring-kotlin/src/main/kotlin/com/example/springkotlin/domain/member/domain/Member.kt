package com.example.springkotlin.domain.member.domain

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime
import javax.persistence.*


@Entity
@Table(name = "member")
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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
        protected set

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime
        protected set

    constructor(email: String, name: String) : this() {
        this.email = email
        this.name = name
    }

    override fun toString(): String {
        return "Member(id=$id, email='$email', name='$name')"
    }

    fun updateName(name: String) {
        this.name = name
    }


}