package com.example.applicationevent.order

import com.example.applicationevent.item.ItemRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class OrderService(
        private val orderRepository: OrderRepository,
        private val itemRepository: ItemRepository,
        private val eventPublisher: ApplicationEventPublisher
) {

    fun order(itemCode: List<String>): Order {
        val items = itemRepository.findByCodeIn(itemCode)
        val orderItems: MutableList<OrderItem> = mutableListOf()
        items.mapTo(orderItems) { OrderItem(it.code, it.price, it.name) }
        val order = orderRepository.save(Order(orderItems))
        eventPublisher.publishEvent(OrderCompletedEvent(itemCode))
        return order
    }
}