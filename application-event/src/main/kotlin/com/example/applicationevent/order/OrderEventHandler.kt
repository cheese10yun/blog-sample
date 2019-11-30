package com.example.applicationevent.order

import com.example.applicationevent.cart.CartRepository
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OrderEventHandler(
        private val cartRepository: CartRepository
) {

    @Async
    @TransactionalEventListener
    fun orderEventHandler(event: OrderCompletedEvent) {
        cartRepository.deleteAllByCodes(codes = event.itemCodes)
    }
}