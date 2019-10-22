package com.example.jpanplus1.order

import com.example.jpanplus1.member.Member
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "orders")
class Order private constructor() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "number", nullable = false)
    lateinit var number: String
        private set

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
        private set

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    lateinit var member: Member

    constructor(member: Member, number: String) : this() {
        this.member = member
        this.number = number
    }

    override fun toString(): String {
        return "Order(id=$id, number='$number', createdAt=$createdAt, updatedAt=$updatedAt)"
    }

}