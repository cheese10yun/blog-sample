package com.service.order

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "orders")
class Order(
    @Column(name = "product_id", nullable = false)
    val productId: String,

    @Column(name = "user_id", nullable = false)
    val userId: String,

    @Column(name = "orderId", nullable = false, unique = true)
    val orderId: String,

    @Column(name = "qty", nullable = false)
    val qty: Int,

    @Column(name = "unit_price", nullable = false)
    val unitPrice: Int,

    @Column(name = "total_price", nullable = false)
    val totalPrice: Int
) : EntityAuditing()

interface OrderRepository : JpaRepository<Order, Long> {

    fun findByOrderId(orderId: String): Order
    fun findByUserId(userId: String): List<Order>
}


@EntityListeners(value = [AuditingEntityListener::class])
@MappedSuperclass
abstract class EntityAuditing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        internal set

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
        internal set

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime
        internal set
}