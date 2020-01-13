package com.example.batch.domain.order.domain

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*


@Entity
@Table(name = "orders")
@Access(AccessType.FIELD) // 용도는 ?
class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "amount", nullable = false)
    lateinit var amount: BigDecimal
        protected set

    @CreatedDate
    @Column(name = "created_at", nullable = true, updatable = false)
    lateinit var createdAt: LocalDateTime
        protected set

    @LastModifiedDate
    @Column(name = "updated_at", nullable = true)
    lateinit var updatedAt: LocalDateTime
        protected set

    fun updatePrice() {
        amount = BigDecimal("1209.11")
    }

}