package com.example.applicationevent.order

import com.example.applicationevent.cart.CartRepository
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class OrderEventHandler(
        private val cartRepository: CartRepository
) {

    @EventListener
    fun orderEventHandler(event: OrderCompletedEvent) {
        cartRepository.deleteAllByCodes(codes = event.itemCodes)
    }

}