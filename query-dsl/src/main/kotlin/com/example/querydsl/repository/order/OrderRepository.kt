package com.example.querydsl.repository.order


import com.example.querydsl.domain.EntityAuditing
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table


@Entity
@Table(name = "orders")
class Order(
    @Column(name = "order_number", nullable = false)
    val orderNumber: String,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "coupon_id", nullable = false)
    val couponId: Long?

) : EntityAuditing()

interface OrderRepository : JpaRepository<Order, Long>