package com.spring.camp.domain

import java.time.LocalDate
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Column
import org.springframework.data.jpa.repository.JpaRepository


@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "customer_name", nullable = false)
    var customerName: String,

    @Column(name = "order_date", nullable = false)
    var orderDate: LocalDate,

    @Column(name = "total_amount", nullable = false)
    var totalAmount: Double,

    @Column(name = "status", nullable = false)
    var status: String
)

interface OrderRepository : JpaRepository<Order, Long>