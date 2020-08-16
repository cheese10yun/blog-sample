package com.example.eventtransaction.order

import com.example.eventtransaction.EmailSenderService
import com.example.eventtransaction.EntityAuditing
import com.example.eventtransaction.cart.CartService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager
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
    private val emailSenderService: EmailSenderService,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun doOrder(dto: OrderRequest) {
        println("doOrder CurrentTransactionName: ${TransactionSynchronizationManager.getCurrentTransactionName()}")
        val order = orderRepository.save(dto.toEntity()) // 1. order 엔티티 영속화
        eventPublisher.publishEvent(OrderCompletedEvent(order)) // 2. 해당상품의 장바구니 제거
//        emailSender.sendOrderEmail(order)
//        cartService.deleteCartWithOrder(order) // 2. 주문 완료 이벤트 발행
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

class OrderCompletedEvent(
    val order: Order
)

@Component
class OrderEventHandler(
    private val cartService: CartService
) {

    //    @TransactionalEventListener
    @Async
    @EventListener
    fun orderCompletedEventListener(event: OrderCompletedEvent) {
        println("orderCompletedEventListener CurrentTransactionName: ${TransactionSynchronizationManager.getCurrentTransactionName()}")
        cartService.deleteCartWithOrder(event.order)
    }
}