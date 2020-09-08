package com.service.order

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice
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

@RestController
@RequestMapping("/orders")
class OrderApi(
    private val orderRepository: OrderRepository
//    private val cartClient: CartClient
) {

    var errorCount = 0

    @GetMapping
    fun getOrders(pageable: Pageable): Page<Order> {
//        println("getOrders 호출")
//        if (errorCount < 2) {
//            println("예외발생 $errorCount 1증가")
//            errorCount++
//            throw RuntimeException("Error")
//        }
//        errorCount = 0 // 초기화
        return orderRepository.findAll(pageable)
    }

    @GetMapping("/carts/{id}")
    fun getCarts(@PathVariable id: Long) {
        if (id == 1L) {
            throw RuntimeException("error")
        }
    }
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

//@FeignClient("cart-service")
//@RibbonClient("cart-service")
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

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun handlerException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(e.message!!), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    data class ErrorResponse(
        val message: String,
        val timestamp: LocalDateTime = LocalDateTime.now()
    )
}