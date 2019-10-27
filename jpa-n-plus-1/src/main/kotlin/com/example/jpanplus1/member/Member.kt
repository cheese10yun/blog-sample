package com.example.jpanplus1.member

import com.example.jpanplus1.copon.Coupon
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

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    var orders: Set<Order> = emptySet()
        private set

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    var coupons: Set<Coupon> = emptySet()
        private set

    constructor(email: String, name: String) : this() {
        this.email = email
        this.name = name
    }

    override fun toString(): String {
        return "Member(id=$id, email='$email', name='$name', createdAt=$createdAt, updatedAt=$updatedAt, orders=$orders, copons=$coupons)"
    }


}