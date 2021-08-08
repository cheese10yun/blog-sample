package com.service.order

import java.time.LocalDateTime
import java.util.UUID
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
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.netflix.ribbon.RibbonClient
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
@RefreshScope
class OrderApi(
    private val orderRepository: OrderRepository,
//    private val cartClient: CartClient,
    @Value("\${message.profile}") val profile: String
) {
    @GetMapping("/profile")
    fun getRepoProfile(): String {
        log.info("==info==")
        log.warn("==warn==")
        log.debug("==debug==")
        return profile
    }


    val log by logger()

    var errorCount = 0

    @GetMapping
    fun getOrders(pageable: Pageable): Page<Order> {
        return orderRepository.findAll(pageable)
    }

//    @GetMapping("/carts/{id}")
//    fun getCarts(@PathVariable id: Long): CartClient.CartResponse {
//
//        orderRepository.findById(1)
//        orderRepository.findById(2)
//        orderRepository.findById(2)
//        orderRepository.findById(3)
//
//        return cartClient.getCart(1)
//    }
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

//@FeignClient(name = "cart-service")
//@RibbonClient(name = "cart-service")
//interface CartClient {
//
//    @GetMapping("/carts/{id}")
//    fun getCart(@PathVariable id: Long): CartResponse
//
//    data class CartResponse(
//        val productId: Long
//    )
//}


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