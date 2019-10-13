package com.example.springkotlin.domain.order

import com.example.springkotlin.domain.member.domain.Member
import com.example.springkotlin.domain.transaction.Transaction

import javax.persistence.*

@Entity
@Table(name = "orders")
class Order protected constructor() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0


    @Column(name = "order_number", nullable = false, updatable = false, unique = true)
    lateinit var number: String
        protected set

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    lateinit var member: Member
        protected set


    @OneToOne
    @JoinColumn(name = "transaction_id", nullable = false, updatable = false)
    lateinit var transaction: Transaction
        protected set

}