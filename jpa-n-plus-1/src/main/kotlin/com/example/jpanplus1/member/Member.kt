package com.example.jpanplus1.member

import com.example.jpanplus1.order.Order
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "member")
class Member private constructor() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "email", nullable = false, unique = true)
    lateinit var email: String
        private set

    @Column(name = "name", nullable = false)
    lateinit var name: String
        private set

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
        private set

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime
        private set

    @OneToMany(mappedBy = "member")
    lateinit var orders: MutableList<Order>
        private set

    constructor(email: String, name: String) : this() {
        this.email = email
        this.name = name
    }
}