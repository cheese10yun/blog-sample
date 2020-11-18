package com.rx.reactive

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "orders")
class Order(
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: OrderStatus
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

enum class OrderStatus {
    COMPLETED,
    FAILED
}

interface OrderRepository : JpaRepository<Order, Long>