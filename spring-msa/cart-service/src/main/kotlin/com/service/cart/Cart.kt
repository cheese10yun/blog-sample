package com.service.cart

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import javax.persistence.*

@RestController
@RequestMapping("/carts")
class CartApi(
    private val cartRepository: CartRepository
) {
    @GetMapping
    fun getCarts(pageable: Pageable) = cartRepository.findAll(pageable)

    @GetMapping("/{id}")
    fun getCart(@PathVariable id: Long) = cartRepository.findById(id).orElseThrow { IllegalArgumentException("$id is not found") }
}

@Entity
@Table(name = "cart")
class Cart(
    @Column(name = "product_id", nullable = false)
    var productId: Long
) : EntityAuditing()

interface CartRepository : JpaRepository<Cart, Long>

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