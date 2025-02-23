package com.example.boot3mongo.order

import com.example.boot3mongo.Boot3MongoApplicationTest
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.findAll

class OrderItemTest(
    private val orderItemRepository: OrderItemRepository
) : Boot3MongoApplicationTest() {

    @Test
    fun `asdasd`() {
        // given
        val orderItems = listOf(
            OrderItem(
                items = listOf(
                    Item(
                        name = "item1",
                        price = 300.toBigDecimal(),
                    ),
                    Item(
                        name = "item2",
                        price = 400.toBigDecimal(),
                    )
                )
            )
        )
        mongoTemplate.insertAll(orderItems)


        val id = orderItems.first().id!!
        // when

        orderItemRepository.updateItems(id)

        // then
        val findAll = mongoTemplate.findAll<OrderItem>()
        println("")


    }
}