package com.example.redis

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository

@Entity(name = "order")
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var productName: String,

    @Column(nullable = false)
    var quantity: Int,

    @Column(nullable = false)
    var price: Double
)


interface OrderRepository : JpaRepository<Order, Long>