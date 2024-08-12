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
    var status: String,

    @Column(name = "shipping_address", nullable = false)
    var shippingAddress: String,

    @Column(name = "billing_address", nullable = false)
    var billingAddress: String,

    @Column(name = "payment_method", nullable = false)
    var paymentMethod: String,

    @Column(name = "shipping_cost", nullable = false)
    var shippingCost: Double,

    @Column(name = "tax_amount", nullable = false)
    var taxAmount: Double,

    @Column(name = "discount", nullable = false)
    var discount: Double,

    @Column(name = "notes")
    var notes: String? = null
)

interface OrderRepository : JpaRepository<Order, Long> {
    fun findByCustomerNameAndStatusAndOrderDate(
        customerName: String,
        status: String,
        orderDate: LocalDate
    ): List<Order>
}