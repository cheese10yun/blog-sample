package com.service.order

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@RestController
@RequestMapping("/orders")
class OrderApi {

    @GetMapping
    fun getOrder(): Order {
        return Order()
    }
}


//@Entity
//@Table(name = "orders")
class Order() {

//    @Column(name = "order_number", nullable = false)
    val orderNumber: String = UUID.randomUUID().toString()
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