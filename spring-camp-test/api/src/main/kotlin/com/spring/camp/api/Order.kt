package com.spring.camp.api

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Table


@Table(name = "orders")
@Entity(name = "orders")
class Order(
    orderNumber: String,
) : EntityAuditing() {
    @Column(name = "order_number", nullable = false)
    val orderNumber: String

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    lateinit var status: OrderStatus
        protected set

    init {
        this.orderNumber = orderNumber
        this.status = OrderStatus.READY
    }

fun updateStatusCompletePayment() {
    // 결게 완료 상태로 바꾸기 위한 유효성 체크 로직 ...
    // 결게 완료 상태로 바꾸는 로직 ...
    this.status = OrderStatus.COMPLETE_PAYMENT
}

fun updateStatusProductPreparation() {
    // 상품 준비 상태로 바꾸기 위한 유효성 체크 로직 ...
    // 상품 준비 상태로 바꾸는 로직 ...
    this.status = OrderStatus.PRODUCT_PREPARATION
}

fun updateStatusDeliveryStarted() {
    // 배송 시작 상태로 바꾸기 위한 유효성 체크 로직 ...
    // 배송 시작 상태로 바꾸는 로직 ...
    this.status = OrderStatus.DELIVERY_STARTED
}

fun updateStatusDeliveryCompleted() {
    // 배송 완료 상태로 바꾸기 위한 유효성 체크 로직 ...
    // 배송 완료 상태로 바꾸는 로직 ...
    this.status = OrderStatus.DELIVERY_COMPLETED
}
}


enum class OrderStatus(description: String) {
    READY("주문"),
    COMPLETE_PAYMENT("결제 완료"),
    PRODUCT_PREPARATION("상품 준비"),
    DELIVERY_STARTED("배송 시작"),
    DELIVERY_COMPLETED("배송 완료")
}

interface OrderRepository : JpaRepository<Order, Long>