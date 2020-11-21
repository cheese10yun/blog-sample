package com.rx.reactive

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

    override fun toString(): String {
        return "Order(status=$status, id=$id)"
    }


}

enum class OrderStatus {
    READY,
    COMPLETED,
    FAILED
}

interface OrderRepository : JpaRepository<Order, Long> {

    @Modifying
    @Query("update Order o set o.status =:orderStatus  where o.id in :orderIds")
    fun updateStatus(
        @Param("orderStatus") orderStatus: OrderStatus,
        @Param("orderIds") ids: List<Long>
    )
}

@Service
class OrderService(
    private val orderRepository: OrderRepository
) {

    @Transactional
    fun updateStatus(
        orderStatus: OrderStatus,
        ids: List<Long>
    ) {
        orderRepository.updateStatus(orderStatus, ids)
    }
}