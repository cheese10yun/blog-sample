package com.example.eventtransaction.order

import com.example.eventtransaction.EntityAuditing
import com.example.eventtransaction.cart.CartService
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import javax.persistence.*

@Entity
@Table(name = "orders")
class Order(
    @Column(name = "product_amount", nullable = false)
    var productAmount: BigDecimal,

    @Column(name = "product_id", nullable = false)
    var productId: Long,

    @Embedded
    var orderer: Orderer
) : EntityAuditing()

@Embeddable
data class Orderer(
    @Column(name = "member_id", nullable = false, updatable = false)
    var memberId: Long,

    @Column(name = "email", nullable = false, updatable = false)
    var email: String
)

interface OrderRepository : JpaRepository<Order, Long>

@RestController
@RequestMapping("orders")
class OrderApi(
    private val orderService: OrderService
) {

    @PostMapping
    fun doOrder(@RequestBody req: OrderRequest) {
        orderService.doOrder(req)
    }
}

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val cartService: CartService,
    private val emailSender: EmailSender
) {

    @Transactional
    fun doOrder(dto: OrderRequest) {
        val order = orderRepository.save(dto.toEntity())
        emailSender.sendOrderEmail(order)
        cartService.deleteCartWithOrder(order)
    }
}

@Service
class EmailSender() {

    fun sendOrderEmail(order: Order) {
        println(
            """
            주문자 이메일 : ${order.orderer.email}
            주문 가격 : ${order.productAmount}
            """.trimIndent()
        )
    }
}

class OrderRequest(
    val productAmount: BigDecimal,
    val productId: Long,
    val orderer: Orderer
) {
    fun toEntity() = Order(
        productAmount = productAmount,
        productId = productId,
        orderer = orderer
    )
}