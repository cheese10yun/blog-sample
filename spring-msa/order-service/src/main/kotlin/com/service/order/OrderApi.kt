package com.service.order

import java.util.UUID
import kotlin.random.Random
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/orders")
@RefreshScope
class OrderApi(
    private val orderRepository: OrderRepository,
    @Value("\${message.profile}") val profile: String,
    private val orderRegistrationService: OrderRegistrationService,
    private val orderFindService: OrderFindService
) {
    val log by logger()

    @GetMapping("/profile")
    fun getRepoProfile(): String {
        log.info("==info==")
        log.warn("==warn==")
        log.debug("==debug==")
        return profile
    }

    @GetMapping
    fun getOrders(pageable: Pageable): Page<Order> {
        return orderRepository.findAll(pageable)
    }

    @PostMapping
    fun register(@RequestBody dto: OrderRegistrationRequest) {
        orderRegistrationService.register(dto)
    }

    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: String) = orderFindService.findByOrderById(orderId)

//    @GetMapping("/users/{userId}")
//    fun getOrderBy(@PathVariable userId: String) = orderFindService.findByUserId(userId)
//        .map { OrderResponse(it) }

    @GetMapping("/users/{userId}")
    fun getOrderByTest(
        @PathVariable userId: String,
        @RequestParam(value = "delay", defaultValue = "0") delay: Int = 0,
        @RequestParam(value = "faultPercentage", defaultValue = "0") faultPercentage: Int = 0
    ): List<OrderResponse> {
        Thread.sleep(delay.toLong())
        val random = Random.nextInt(0, 100)
        if (faultPercentage > random) {
            throw RuntimeException("faultPercentage Error...")
        }

        return orderFindService.findByUserId(userId)
            .map { OrderResponse(it) }
    }
}

@Service
class OrderRegistrationService(
    private val orderRepository: OrderRepository
) {

    @Transactional
    fun register(dto: OrderRegistrationRequest) {
        orderRepository.save(dto.toEntity(UUID.randomUUID().toString()))
    }
}

@Service
@Transactional(readOnly = true)
class OrderFindService(
    private val orderRepository: OrderRepository
) {
    fun findByOrderById(orderId: String) = orderRepository.findByOrderId(orderId)

    fun findByUserId(userId: String) = orderRepository.findByUserId(userId)

}


class OrderResponse(order: Order) {
    val productId = order.productId
    val userId = order.userId
    val orderId = order.orderId
    val qty = order.qty
    val unitPrice = order.unitPrice
    val totalPrice = order.totalPrice
}

data class OrderRegistrationRequest(
    val productId: String,
    val orderId: String,
    val qty: Int,
    val unitPrice: Int,
) {
    fun toEntity(userId: String) = Order(
        productId = productId,
        userId = userId,
        orderId = orderId,
        qty = qty,
        unitPrice = unitPrice,
        totalPrice = qty * unitPrice
    )
}