package com.example.applicationevent

import com.example.applicationevent.cart.Cart
import com.example.applicationevent.cart.CartRepository
import com.example.applicationevent.item.Item
import com.example.applicationevent.item.ItemRepository
import com.example.applicationevent.order.Order
import com.example.applicationevent.order.OrderItem
import com.example.applicationevent.order.OrderRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class AppRunner(
        private val orderRepository: OrderRepository,
        private val cartRepository: CartRepository,
        private val itemRepository: ItemRepository
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {


        itemRepository.saveAll(
                listOf(
                        Item("C001", BigDecimal(1000), "양말"),
                        Item("C002", BigDecimal(2000), "바지"),
                        Item("C003", BigDecimal(3000), "바지")
                )
        )


        val productions = listOf(
                OrderItem("C001", BigDecimal(1000), "양말"),
                OrderItem("C002", BigDecimal(2000), "바지"),
                OrderItem("C003", BigDecimal(3000), "바지")
        )

        orderRepository.save(Order(productions))
        cartRepository.saveAll(listOf(
                Cart("C001"),
                Cart("C002"),
                Cart("C003")
        ))
    }
}