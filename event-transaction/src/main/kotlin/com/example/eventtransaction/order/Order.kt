package com.example.eventtransaction.order

import com.example.eventtransaction.EntityAuditing
import com.example.eventtransaction.cart.CartService
import com.example.eventtransaction.member.Member
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
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
    private val emailSenderService: EmailSenderService,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun doOrder(dto: OrderRequest) {
        val order = orderRepository.save(dto.toEntity())
        eventPublisher.publishEvent(OrderCompletedEvent(order))
//        emailSender.sendOrderEmail(order)
//        cartService.deleteCartWithOrder(order)
    }
}

@Service
class EmailSenderService {

    /**
     * 외부 인프라 서비스를 호출한다고 가정한다
     */
    fun sendOrderEmail(order: Order) {
        println(
            """
            주문자 이메일 : ${order.orderer.email}
            주문 가격 : ${order.productAmount}
            """.trimIndent()
        )
    }

    /**
     * 외부 인프라 서비스를 호출한다고 가정한다
     */
    fun sendSignUpEmail(member: Member) {
        println(
            """
                ${member.name} + " 님 회원가입을 축하드립니다."
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

class OrderCompletedEvent(
    val order: Order
)

@Component
class OrderEventHandler(
    private val cartService: CartService,
    private val emailSenderService: EmailSenderService
) {

    //    @TransactionalEventListener
    @Async
    @EventListener
    fun orderCompletedEventListener(event: OrderCompletedEvent) {
        emailSenderService.sendOrderEmail(event.order)
        cartService.deleteCartWithOrder(event.order)
        throw RuntimeException("runtime exception ....")
    }
}