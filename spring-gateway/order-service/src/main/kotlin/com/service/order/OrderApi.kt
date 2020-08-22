package com.service.order

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.cloud.netflix.ribbon.RibbonClient
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@RestController
@RequestMapping("/orders")
class OrderApi(
    private val orderRepository: OrderRepository,
    private val cartClient: CartClient
) {

    @GetMapping
    fun getOrders(pageable: Pageable) = orderRepository.findAll(pageable)

    @GetMapping("/carts")
    fun getCarts() = cartClient.getCart()
}


@Entity
@Table(name = "orders")
class Order(
    @Column(name = "product_id", nullable = false)
    val productId: Long
) : EntityAuditing() {
    @Column(name = "order_number", nullable = false)
    val orderNumber: String = UUID.randomUUID().toString()
}

interface OrderRepository : JpaRepository<Order, Long>

@FeignClient("cart-service")
@RibbonClient("cart-service")
interface CartClient {

    @GetMapping("/carts")
    fun getCart(): CartResponse

    data class CartResponse(
        val memberId: Long
    )
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