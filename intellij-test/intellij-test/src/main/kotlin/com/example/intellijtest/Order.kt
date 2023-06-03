package com.example.intellijtest

import jakarta.persistence.Column
import jakarta.persistence.Entity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

@Entity(name = "orders")
class Order(
    @Column(name = "shop_id", nullable = false)
    val shopId: Long,
    @Column(name = "customer_name", nullable = false)
    val customerName: String,
    @Column(name = "phone_number", nullable = false)
    val phoneNumber: String,
    @Column(name = "email", nullable = false)
    val email: String,
    @Column(name = "order_time", nullable = false)
    val orderTime: LocalDateTime,
    @Column(name = "total_price", nullable = false)
    val totalPrice: Double,
    @Column(name = "is_completed", nullable = false)
    val isCompleted: Boolean,
    @Column(name = "delivery_address", nullable = false)
    val deliveryAddress: String,
    @Column(name = "delivery_time", nullable = false)
    val deliveryTime: LocalDateTime,
    @Column(name = "payment_method", nullable = false)
    val paymentMethod: String,
    @Column(name = "coupon_code", nullable = false)
    val couponCode: String,
    @Column(name = "discount_amount", nullable = false)
    val discountAmount: Double,
    @Column(name = "order_items", nullable = false)
    val orderItems: String,
    @Column(name = "additional_notes", nullable = false)
    val additionalNotes: String,
) : EntityAuditing()

interface OrderRepository : JpaRepository<Order, Long>